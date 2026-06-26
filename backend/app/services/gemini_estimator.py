import json
import os
from typing import Any
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

GEMINI_BASE = "https://generativelanguage.googleapis.com/v1beta/models"
DEFAULT_GEMINI_MODEL = "gemini-2.0-flash"


def _gemini_request(prompt: str, timeout: int = 20) -> str:
    api_key = os.getenv("GEMINI_API_KEY", "").strip()
    if not api_key:
        raise RuntimeError("GEMINI_API_KEY is not configured")

    model = os.getenv("GEMINI_MODEL", DEFAULT_GEMINI_MODEL)
    url = f"{GEMINI_BASE}/{model}:generateContent?key={api_key}"

    body = {
        "contents": [{"parts": [{"text": prompt}]}],
        "generationConfig": {
            "responseMimeType": "application/json",
            "temperature": 0.1,
        },
    }

    req = Request(
        url,
        data=json.dumps(body).encode("utf-8"),
        headers={"Content-Type": "application/json", "User-Agent": "CalorAI/1.0"},
        method="POST",
    )
    try:
        with urlopen(req, timeout=timeout) as resp:
            payload = json.loads(resp.read().decode("utf-8"))
    except HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"Gemini HTTP {exc.code}: {detail}") from exc
    except (URLError, TimeoutError, ValueError) as exc:
        raise RuntimeError(f"Gemini request failed: {exc}") from exc

    try:
        return payload["candidates"][0]["content"]["parts"][0]["text"]
    except (KeyError, IndexError) as exc:
        raise RuntimeError(f"Unexpected Gemini response: {payload}") from exc


def _extract_json(raw: str) -> dict[str, Any] | None:
    text = (raw or "").strip()
    if text.startswith("```"):
        text = text.strip("`").replace("json", "", 1).strip()
    try:
        return json.loads(text)
    except json.JSONDecodeError:
        s, e = text.find("{"), text.rfind("}")
        if s == -1 or e <= s:
            return None
        try:
            return json.loads(text[s: e + 1])
        except json.JSONDecodeError:
            return None


def _normalize_item(item: dict[str, Any]) -> dict[str, Any] | None:
    parsed_food = str(item.get("parsed_food") or "").strip().lower()
    if not parsed_food:
        return None

    estimated_grams = float(item.get("estimated_grams") or 0.0)
    kcal_per_gram = float(item.get("kcal_per_gram") or 0.0)
    calories = float(item.get("calories") or 0.0)

    raw_range = item.get("calorie_range") or []
    if isinstance(raw_range, list) and len(raw_range) >= 2:
        low, high = float(raw_range[0]), float(raw_range[1])
    else:
        low, high = calories * 0.85, calories * 1.15
    low, high = min(low, calories), max(high, calories)

    return {
        "note_part": str(item.get("note_part") or parsed_food),
        "parsed_food": parsed_food,
        "quantity": round(max(float(item.get("quantity") or 1.0), 0.1), 2),
        "unit": (str(item.get("unit") or "").strip().lower() or None),
        "estimated_grams": round(max(estimated_grams, 1.0), 1),
        "kcal_per_gram": round(max(kcal_per_gram, 0.01), 3),
        "calories": round(max(calories, 1.0), 1),
        "calorie_range": [round(max(low, 0.0), 1), round(max(high, 0.0), 1)],
        "protein_g": round(max(float(item.get("protein_g") or 0.0), 0.0), 1),
        "carbs_g": round(max(float(item.get("carbs_g") or 0.0), 0.0), 1),
        "fat_g": round(max(float(item.get("fat_g") or 0.0), 0.0), 1),
        "nutrition_source": "gemini_ai",
        "source_label": str(item.get("source_label") or "").strip() or None,
        "matched_description": str(item.get("assumption") or "").strip() or None,
    }


def estimate_with_gemini(note: str) -> dict[str, Any] | None:
    prompt = f"""You are a registered-dietitian-level nutrition expert. Calculate the exact calories and macros for this meal using your nutrition knowledge.

RULES:
- All JSON values must be pre-computed numbers. Never write expressions like "200 * 1.65" — write the result "330".
- calories = estimated_grams × kcal_per_gram. Verify this arithmetic before writing it.
- If a weight is stated explicitly (e.g. "200g"), use that exact weight, do not substitute.
- Set calorie_range narrow (±5%) when the input was precise, wider (±20%) when vague.
- In source_label name the specific database entry you are drawing from (e.g. "USDA FoodData Central — Chicken breast, roasted, meat only").
- In assumption explain what you assumed about portion size, cooking method, or brand.

Return ONLY this JSON schema, no markdown, no text outside the JSON:
{{
  "items": [
    {{
      "note_part": "exact phrase from input referring to this food",
      "parsed_food": "standardized food name with preparation method",
      "quantity": <number of units>,
      "unit": "g/ml/piece/slice/cup or null",
      "estimated_grams": <total weight in grams>,
      "kcal_per_gram": <calories per gram — must match USDA values>,
      "calories": <estimated_grams × kcal_per_gram>,
      "calorie_range": [<low estimate>, <high estimate>],
      "protein_g": <grams of protein>,
      "carbs_g": <grams of carbohydrates>,
      "fat_g": <grams of fat>,
      "source_label": "specific USDA FoodData Central entry or database used",
      "assumption": "what you assumed about portion, cooking method, or brand"
    }}
  ],
  "total_calories": <sum of all item calories>,
  "total_calorie_range": [<sum of lows>, <sum of highs>]
}}

Meal description: {note}"""

    content = _gemini_request(prompt, timeout=20)
    parsed = _extract_json(content)
    if not parsed:
        raise RuntimeError(f"Gemini returned non-JSON: {content[:200]}")

    items: list[dict[str, Any]] = []
    for raw_item in (parsed.get("items") or []):
        if not isinstance(raw_item, dict):
            continue
        normalized = _normalize_item(raw_item)
        if normalized:
            items.append(normalized)

    if not items:
        raise RuntimeError("Gemini response contained no valid items")

    total_calories = round(sum(float(i["calories"]) for i in items), 1)
    total_low = round(sum(float(i["calorie_range"][0]) for i in items), 1)
    total_high = round(sum(float(i["calorie_range"][1]) for i in items), 1)
    total_protein = round(sum(float(i["protein_g"]) for i in items), 1)
    total_carbs = round(sum(float(i["carbs_g"]) for i in items), 1)
    total_fat = round(sum(float(i["fat_g"]) for i in items), 1)

    source_strings: list[str] = []
    seen: set[str] = set()
    for item in items:
        lbl = item.get("source_label") or ""
        if lbl and lbl not in seen:
            seen.add(lbl)
            source_strings.append(lbl)

    return {
        "note": note,
        "items": items,
        "total_calories": total_calories,
        "total_calorie_range": [total_low, total_high],
        "total_protein_g": total_protein,
        "total_carbs_g": total_carbs,
        "total_fat_g": total_fat,
        "sources": source_strings,
        "web_grounded": False,
        "provider": "gemini",
    }

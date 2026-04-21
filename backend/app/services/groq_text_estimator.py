import json
import os
from typing import Any
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions"
DEFAULT_MODEL = "llama-3.1-8b-instant"


def _extract_json_payload(raw_text: str) -> dict[str, Any] | None:
    text = (raw_text or "").strip()
    if not text:
        return None

    if text.startswith("```"):
        text = text.strip("`")
        text = text.replace("json", "", 1).strip()

    try:
        return json.loads(text)
    except json.JSONDecodeError:
        start = text.find("{")
        end = text.rfind("}")
        if start == -1 or end == -1 or end <= start:
            return None
        try:
            return json.loads(text[start : end + 1])
        except json.JSONDecodeError:
            return None


def _normalize_item(item: dict[str, Any]) -> dict[str, Any] | None:
    parsed_food = str(item.get("parsed_food") or "").strip().lower()
    if not parsed_food:
        return None

    quantity = float(item.get("quantity") or 1.0)
    estimated_grams = float(item.get("estimated_grams") or 0.0)
    kcal_per_gram = float(item.get("kcal_per_gram") or 0.0)
    calories = float(item.get("calories") or 0.0)

    raw_range = item.get("calorie_range") or []
    if isinstance(raw_range, list) and len(raw_range) >= 2:
        low = float(raw_range[0])
        high = float(raw_range[1])
    else:
        low = calories * 0.85
        high = calories * 1.15

    assumption = str(item.get("assumption") or "").strip()

    return {
        "note_part": str(item.get("note_part") or parsed_food),
        "parsed_food": parsed_food,
        "quantity": round(max(quantity, 0.1), 2),
        "unit": (str(item.get("unit") or "").strip().lower() or None),
        "estimated_grams": round(max(estimated_grams, 1.0), 1),
        "kcal_per_gram": round(max(kcal_per_gram, 0.01), 3),
        "calories": round(max(calories, 1.0), 1),
        "calorie_range": [round(max(low, 0.0), 1), round(max(high, 0.0), 1)],
        "nutrition_source": "groq_ai",
        "matched_description": assumption or None,
    }


def estimate_with_groq(note: str) -> dict[str, Any] | None:
    api_key = os.getenv("GROQ_API_KEY")
    if not api_key:
        raise RuntimeError("GROQ_API_KEY is not configured")

    model = (os.getenv("GROQ_MODEL") or DEFAULT_MODEL).strip() or DEFAULT_MODEL
    prompt = (
        "You are a nutrition estimation assistant. Return ONLY valid JSON. "
        "Given a meal note, extract food items and estimate calories. "
        "Include a transparent low/high calorie range per item. "
        "JSON schema: "
        "{\"items\":[{\"note_part\":str,\"parsed_food\":str,\"quantity\":number,\"unit\":str|null,\"estimated_grams\":number,\"kcal_per_gram\":number,\"calories\":number,\"calorie_range\":[number,number],\"assumption\":str}],\"total_calories\":number,\"total_calorie_range\":[number,number]} "
        "Use realistic values. If uncertain, broaden ranges and explain assumptions briefly. "
        f"Meal note: {note}"
    )

    body = {
        "model": model,
        "messages": [
            {
                "role": "system",
                "content": "Return strict JSON only. No markdown. No extra text.",
            },
            {
                "role": "user",
                "content": prompt,
            },
        ],
        "temperature": 0.2,
        "response_format": {"type": "json_object"},
    }

    req = Request(
        GROQ_API_URL,
        data=json.dumps(body).encode("utf-8"),
        headers={
            "Content-Type": "application/json",
            "Authorization": f"Bearer {api_key}",
            "Accept": "application/json",
            "User-Agent": "CalorAI/1.0",
        },
        method="POST",
    )

    try:
        with urlopen(req, timeout=12) as response:
            payload = json.loads(response.read().decode("utf-8"))
    except HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"Groq HTTP {exc.code}: {detail}") from exc
    except (URLError, TimeoutError, ValueError) as exc:
        raise RuntimeError(f"Groq request failed: {exc}") from exc

    content = (
        payload.get("choices", [{}])[0]
        .get("message", {})
        .get("content", "")
    )

    parsed = _extract_json_payload(content)
    if not parsed:
        preview = (content or "").strip().replace("\n", " ")[:220]
        raise RuntimeError(f"Groq returned non-JSON output: {preview}")

    raw_items = parsed.get("items") or []
    items: list[dict[str, Any]] = []
    for raw_item in raw_items:
        if not isinstance(raw_item, dict):
            continue
        normalized = _normalize_item(raw_item)
        if normalized is not None:
            items.append(normalized)

    if not items:
        raise RuntimeError("Groq response did not contain any valid items")

    total_calories = round(sum(float(item["calories"]) for item in items), 1)
    total_low = round(sum(float(item["calorie_range"][0]) for item in items), 1)
    total_high = round(sum(float(item["calorie_range"][1]) for item in items), 1)

    return {
        "note": note,
        "items": items,
        "total_calories": total_calories,
        "total_calorie_range": [total_low, total_high],
    }

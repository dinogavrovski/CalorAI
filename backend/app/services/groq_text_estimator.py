import json
import os
from typing import Any
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

from app.services.tavily_search import search_nutrition_for_foods

GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions"
FAST_MODEL = "llama-3.1-8b-instant"       # Step 1: cheap parse
DEFAULT_MODEL = "llama-3.3-70b-versatile"  # Step 3: final estimation


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _groq_request(messages: list[dict], model: str, temperature: float = 0.1, timeout: int = 15) -> str:
    api_key = os.getenv("GROQ_API_KEY")
    if not api_key:
        raise RuntimeError("GROQ_API_KEY is not configured")

    body = {
        "model": model,
        "messages": messages,
        "temperature": temperature,
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
        with urlopen(req, timeout=timeout) as response:
            payload = json.loads(response.read().decode("utf-8"))
    except HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"Groq HTTP {exc.code}: {detail}") from exc
    except (URLError, TimeoutError, ValueError) as exc:
        raise RuntimeError(f"Groq request failed: {exc}") from exc

    return (
        payload.get("choices", [{}])[0]
        .get("message", {})
        .get("content", "")
    )


def _extract_json_payload(raw_text: str) -> dict[str, Any] | None:
    text = (raw_text or "").strip()
    if not text:
        return None
    if text.startswith("```"):
        text = text.strip("`").replace("json", "", 1).strip()
    try:
        return json.loads(text)
    except json.JSONDecodeError:
        start, end = text.find("{"), text.rfind("}")
        if start == -1 or end <= start:
            return None
        try:
            return json.loads(text[start: end + 1])
        except json.JSONDecodeError:
            return None


# ---------------------------------------------------------------------------
# Step 1 — Fast parse: extract food items from the meal description
# ---------------------------------------------------------------------------

def _parse_food_items(note: str) -> list[str]:
    """
    Use the fast 8B model to identify distinct food items in the meal text.
    Returns a list of standardised food name strings for Tavily searching.
    """
    messages = [
        {
            "role": "system",
            "content": (
                "Extract every distinct food item from the meal description. "
                "For each item include preparation method and any quantity/weight stated. "
                "Return ONLY a JSON object: {\"foods\": [\"food1\", \"food2\", ...]}"
            ),
        },
        {"role": "user", "content": note},
    ]
    try:
        content = _groq_request(messages, model=FAST_MODEL, timeout=8)
        parsed = _extract_json_payload(content)
        foods = parsed.get("foods") if parsed else None
        if isinstance(foods, list):
            return [str(f).strip() for f in foods if f]
    except Exception:
        pass
    # Fallback: treat the whole note as one item
    return [note]


# ---------------------------------------------------------------------------
# Step 3 — Final estimation with Tavily context
# ---------------------------------------------------------------------------

def _build_web_context(search_results: list[dict]) -> str:
    """Format Tavily search results as a readable context block for the prompt."""
    if not search_results:
        return ""
    lines = ["=== REAL NUTRITION DATA FROM THE WEB ==="]
    for res in search_results:
        lines.append(f"\n[Food: {res['food']}]")
        if res.get("answer"):
            lines.append(f"Summary: {res['answer']}")
        for src in res.get("sources", [])[:2]:
            if src.get("snippet"):
                lines.append(f"Source ({src['title']}): {src['snippet']}")
    lines.append("\n=== USE THE ABOVE DATA FOR YOUR CALORIE CALCULATIONS ===")
    return "\n".join(lines)


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

    assumption = str(item.get("assumption") or "").strip()
    protein_g = float(item.get("protein_g") or 0.0)
    carbs_g = float(item.get("carbs_g") or 0.0)
    fat_g = float(item.get("fat_g") or 0.0)
    source_label = str(item.get("source_label") or "").strip()

    return {
        "note_part": str(item.get("note_part") or parsed_food),
        "parsed_food": parsed_food,
        "quantity": round(max(float(item.get("quantity") or 1.0), 0.1), 2),
        "unit": (str(item.get("unit") or "").strip().lower() or None),
        "estimated_grams": round(max(estimated_grams, 1.0), 1),
        "kcal_per_gram": round(max(kcal_per_gram, 0.01), 3),
        "calories": round(max(calories, 1.0), 1),
        "calorie_range": [round(max(low, 0.0), 1), round(max(high, 0.0), 1)],
        "protein_g": round(max(protein_g, 0.0), 1),
        "carbs_g": round(max(carbs_g, 0.0), 1),
        "fat_g": round(max(fat_g, 0.0), 1),
        "nutrition_source": "web_grounded" if source_label else "groq_ai",
        "source_label": source_label or None,
        "matched_description": assumption or None,
    }


# ---------------------------------------------------------------------------
# Main entry point
# ---------------------------------------------------------------------------

def estimate_with_groq(note: str) -> dict[str, Any] | None:
    model = (os.getenv("GROQ_MODEL") or DEFAULT_MODEL).strip() or DEFAULT_MODEL
    has_tavily = bool(os.getenv("TAVILY_API_KEY", "").strip())

    # ── Step 1: parse food items (fast) ─────────────────────────────────────
    food_items = _parse_food_items(note)

    # ── Step 2: parallel Tavily web search ───────────────────────────────────
    search_results: list[dict] = []
    all_sources: list[dict] = []   # {title, url} pairs for citations
    if has_tavily:
        search_results = search_nutrition_for_foods(food_items)
        for res in search_results:
            for src in res.get("sources", []):
                if src.get("url") and src.get("title"):
                    entry = {"title": src["title"], "url": src["url"]}
                    if entry not in all_sources:
                        all_sources.append(entry)

    web_context = _build_web_context(search_results)

    # ── Step 3: final AI estimation ──────────────────────────────────────────
    system_prompt = """You are a precise nutrition estimation assistant with access to real web search results.

Your task: analyze a meal description and return accurate calorie/macro estimates.

RULES:
- If web search data is provided above, use those exact values. Do not substitute your own memory.
- calories = estimated_grams × kcal_per_gram — always verify this arithmetic before writing it.
- If a weight is stated explicitly (e.g. "200g"), use that exact weight.
- Set calorie_range narrow (±5%) when data was specific, wider (±20%) when vague.
- In source_label, name the specific website or source the data came from (e.g. "USDA FoodData Central via healthline.com").
- CRITICAL: All JSON values must be pre-computed numbers. NEVER write arithmetic expressions like "200 * 1.65" or "165 / 100". Compute the result yourself and write only the final number (e.g. 330, not "200 * 1.65").
- Return ONLY valid JSON. No markdown, no text outside the JSON."""

    user_prompt = f"""{web_context}

Analyze this meal and return a JSON object with this exact schema:
{{
  "items": [
    {{
      "note_part": "exact phrase from input",
      "parsed_food": "standardized food name with preparation method",
      "quantity": <number>,
      "unit": "g/ml/piece/slice/cup or null",
      "estimated_grams": <total weight in grams>,
      "kcal_per_gram": <calories per gram>,
      "calories": <estimated_grams × kcal_per_gram>,
      "calorie_range": [<low>, <high>],
      "protein_g": <grams of protein>,
      "carbs_g": <grams of carbohydrates>,
      "fat_g": <grams of fat>,
      "source_label": "specific source website or database used for this item",
      "assumption": "what you assumed about portion, cooking method, or brand"
    }}
  ],
  "total_calories": <sum of item calories>,
  "total_calorie_range": [<sum of lows>, <sum of highs>]
}}

Meal description: {note}"""

    content = _groq_request(
        messages=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_prompt},
        ],
        model=model,
        timeout=25,
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

    total_calories = round(sum(float(i["calories"]) for i in items), 1)
    total_low = round(sum(float(i["calorie_range"][0]) for i in items), 1)
    total_high = round(sum(float(i["calorie_range"][1]) for i in items), 1)
    total_protein = round(sum(float(i["protein_g"]) for i in items), 1)
    total_carbs = round(sum(float(i["carbs_g"]) for i in items), 1)
    total_fat = round(sum(float(i["fat_g"]) for i in items), 1)

    # Build deduplicated source list: prefer Tavily URLs, fall back to item source_labels
    source_strings: list[str] = []
    seen: set[str] = set()

    # Real URLs from Tavily
    for src in all_sources:
        label = f"{src['title']} — {src['url']}"
        if label not in seen:
            seen.add(label)
            source_strings.append(label)

    # Fallback: source_label strings from individual items (no URL)
    if not source_strings:
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
        "web_grounded": has_tavily,
    }

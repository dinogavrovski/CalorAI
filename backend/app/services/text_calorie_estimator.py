import re
from typing import Dict

from app.services.food_database import lookup_usda_kcal_per_gram_query


DEFAULT_KCAL_PER_GRAM = 2.0
DEFAULT_SERVING_GRAMS = 150.0

WEIGHT_TO_GRAMS = {
    "g": 1.0,
    "gram": 1.0,
    "grams": 1.0,
    "kg": 1000.0,
    "kilogram": 1000.0,
    "kilograms": 1000.0,
    "oz": 28.3495,
    "ounce": 28.3495,
    "ounces": 28.3495,
    "lb": 453.592,
    "lbs": 453.592,
    "pound": 453.592,
    "pounds": 453.592,
}

QUANTITY_WORDS = {
    "a": 1.0,
    "an": 1.0,
    "one": 1.0,
    "two": 2.0,
    "three": 3.0,
    "four": 4.0,
    "five": 5.0,
    "half": 0.5,
    "quarter": 0.25,
}

UNIT_GRAMS = {
    "slice": 110.0,
    "piece": 80.0,
    "cup": 240.0,
    "bowl": 350.0,
    "burger": 180.0,
    "sandwich": 180.0,
    "taco": 100.0,
    "egg": 50.0,
    "serving": DEFAULT_SERVING_GRAMS,
}

PROTECTED_FOOD_PATTERNS = [
    r"fish\s*(?:and|&)\s*chips",
    r"mac\s*(?:and|&)\s*cheese",
    r"peanut\s*butter\s*(?:and|&)\s*jelly",
    r"spaghetti\s*(?:and|&)\s*meatballs",
]


def _parse_quantity(token: str | None) -> float:
    if not token:
        return 1.0

    t = token.strip().lower()
    if t in QUANTITY_WORDS:
        return QUANTITY_WORDS[t]

    if re.fullmatch(r"\d+/\d+", t):
        num, den = t.split("/", 1)
        den_val = float(den)
        return float(num) / den_val if den_val else 1.0

    try:
        return float(t)
    except ValueError:
        return 1.0


def _normalize_unit(token: str | None) -> str | None:
    if not token:
        return None

    t = token.strip().lower()
    if t.endswith("es") and t[:-2] in UNIT_GRAMS:
        return t[:-2]
    if t.endswith("s") and t[:-1] in UNIT_GRAMS:
        return t[:-1]
    if t in UNIT_GRAMS:
        return t

    return None


def _clean_food_text(food_text: str) -> str:
    text = food_text.strip().lower()
    text = re.sub(r"^[^a-z0-9]+", "", text)
    text = re.sub(r"^(a|an|the)\s+", "", text)
    text = re.sub(r"\s+", " ", text)
    return text.strip()


def _split_meal_note(note: str) -> list[str]:
    text = note.strip().lower()
    if not text:
        return []

    protected_map: dict[str, str] = {}
    for idx, pattern in enumerate(PROTECTED_FOOD_PATTERNS):
        token = f"__protected_{idx}__"

        def _replacement(match: re.Match) -> str:
            protected_map[token] = match.group(0)
            return token

        text = re.sub(pattern, _replacement, text)

    parts = re.split(r"\s*(?:,|\+|&|\band\b|\bwith\b|\balongside\b|\bplus\b)\s*", text)
    cleaned_parts: list[str] = []
    for part in parts:
        p = part.strip()
        if not p:
            continue

        for token, phrase in protected_map.items():
            p = p.replace(token, phrase)

        cleaned_parts.append(p)

    return cleaned_parts


def _try_parse_explicit_weight(text: str) -> Dict | None:
    # Examples: "250g salmon", "250 g of salmon", "1/2 lb burger", "8 oz steak"
    leading_weight = re.compile(
        r"^(?P<qty>\d+(?:\.\d+)?|\d+/\d+)\s*(?P<unit>g|gram|grams|kg|kilogram|kilograms|oz|ounce|ounces|lb|lbs|pound|pounds)\s+(?:of\s+)?(?P<food>.+)$"
    )
    trailing_weight = re.compile(
        r"^(?P<food>.+?)\s+(?P<qty>\d+(?:\.\d+)?|\d+/\d+)\s*(?P<unit>g|gram|grams|kg|kilogram|kilograms|oz|ounce|ounces|lb|lbs|pound|pounds)$"
    )

    match = leading_weight.match(text) or trailing_weight.match(text)
    if not match:
        return None

    quantity = _parse_quantity(match.group("qty"))
    unit = (match.group("unit") or "").strip().lower()
    food = _clean_food_text(match.group("food") or text)

    if unit not in WEIGHT_TO_GRAMS:
        return None

    explicit_grams = max(quantity, 0.1) * WEIGHT_TO_GRAMS[unit]
    return {
        "quantity": round(max(quantity, 0.1), 4),
        "unit": "gram",
        "food": food,
        "explicit_grams": explicit_grams,
    }


def parse_food_note(note: str) -> Dict:
    text = note.strip().lower()
    text = re.sub(r"[,.;!?]", " ", text)
    text = re.sub(r"\s+", " ", text)

    explicit_weight = _try_parse_explicit_weight(text)
    if explicit_weight is not None:
        return explicit_weight

    pattern = re.compile(
        r"^(?:(?P<qty>\d+(?:\.\d+)?|\d+/\d+|a|an|one|two|three|four|five|half|quarter)\s+)?"
        r"(?:(?P<unit>slices?|pieces?|cups?|bowls?|burgers?|sandwich(?:es)?|tacos?|eggs?|servings?)\s+)?"
        r"(?:of\s+)?(?P<food>.+)$"
    )
    match = pattern.match(text)

    if not match:
        return {
            "quantity": 1.0,
            "unit": None,
            "food": _clean_food_text(text),
            "explicit_grams": None,
        }

    quantity = _parse_quantity(match.group("qty"))
    unit = _normalize_unit(match.group("unit"))
    food = _clean_food_text(match.group("food") or text)

    return {
        "quantity": quantity,
        "unit": unit,
        "food": food,
        "explicit_grams": None,
    }


def _estimate_grams(food: str, quantity: float, unit: str | None, explicit_grams: float | None = None) -> float:
    if explicit_grams is not None:
        return max(explicit_grams, 1.0)

    if unit == "slice" and "pizza" in food:
        return quantity * 120.0

    if unit:
        grams_per_unit = UNIT_GRAMS.get(unit, DEFAULT_SERVING_GRAMS)
        return quantity * grams_per_unit

    # Handle notes like "half a cheeseburger" with no explicit unit.
    if "burger" in food:
        return quantity * 180.0

    if "pizza" in food:
        return quantity * 300.0

    return quantity * DEFAULT_SERVING_GRAMS


def _estimate_single_note(note_part: str) -> Dict:
    parsed = parse_food_note(note_part)

    food_name = parsed["food"]
    quantity = max(parsed["quantity"], 0.1)
    unit = parsed["unit"]
    explicit_grams = parsed.get("explicit_grams")
    estimated_grams = _estimate_grams(food_name, quantity, unit, explicit_grams=explicit_grams)

    db_lookup = lookup_usda_kcal_per_gram_query(food_name)
    if db_lookup.get("found"):
        kcal_per_gram = float(db_lookup["kcal_per_gram"])
        source = "usda_fdc"
    else:
        kcal_per_gram = DEFAULT_KCAL_PER_GRAM
        source = "default_fallback"

    calories = estimated_grams * kcal_per_gram
    low = calories * 0.85
    high = calories * 1.15

    return {
        "note_part": note_part,
        "parsed_food": food_name,
        "quantity": round(quantity, 2),
        "unit": unit,
        "estimated_grams": round(estimated_grams, 1),
        "kcal_per_gram": round(kcal_per_gram, 3),
        "calories": round(calories, 1),
        "calorie_range": [round(low, 1), round(high, 1)],
        "nutrition_source": source,
        "matched_description": db_lookup.get("matched_description"),
    }


def estimate_from_text_note(note: str) -> Dict:
    note_parts = _split_meal_note(note)
    if not note_parts:
        note_parts = [note.strip().lower()]

    items = [_estimate_single_note(part) for part in note_parts if part.strip()]

    total_calories = sum(float(item["calories"]) for item in items)
    total_low = sum(float(item["calorie_range"][0]) for item in items)
    total_high = sum(float(item["calorie_range"][1]) for item in items)

    return {
        "note": note,
        "items": items,
        "total_calories": round(total_calories, 1),
        "total_calorie_range": [round(total_low, 1), round(total_high, 1)],
    }

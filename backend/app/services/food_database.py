import json
import os
from difflib import get_close_matches
from functools import lru_cache
from pathlib import Path
from typing import Dict, Optional
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode
from urllib.request import urlopen


USDA_SEARCH_URL = "https://api.nal.usda.gov/fdc/v1/foods/search"
TYPO_PHRASE_CUTOFF = 0.80
TYPO_TOKEN_CUTOFF = 0.80

PREFERRED_DATA_TYPE_SCORE = {
    "Foundation": 4.0,
    "SR Legacy": 3.5,
    "Survey (FNDDS)": 3.0,
    "Branded": 0.5,
}

LOW_SIGNAL_TOKENS = {
    "cooked",
    "raw",
    "fresh",
    "large",
    "small",
    "medium",
    "food",
}

QUERY_NOISE_TOKENS = {
    "a",
    "an",
    "the",
    "of",
    "with",
    "and",
}

COMMON_TYPO_MAP = {
    "bef": "beef",
    "beaf": "beef",
    "chiken": "chicken",
    "chikn": "chicken",
    "riece": "rice",
    "rise": "rice",
    "reis": "rice",
    "frise": "fries",
    "frys": "fries",
    "tacco": "taco",
    "taccos": "tacos",
    "salomn": "salmon",
    "peproni": "pepperoni",
}

CHAIN_BRAND_KEYWORDS = {
    "taco bell",
    "mcdonald",
    "burger king",
    "wendy",
    "kfc",
    "subway",
    "domino",
    "pizza hut",
    "starbucks",
}

BRAND_INTENT_KEYWORDS = {
    "mcdonald",
    "mcdonalds",
    "taco bell",
    "burger king",
    "wendy",
    "kfc",
    "subway",
    "domino",
    "pizza hut",
    "chipotle",
    "starbucks",
    "big mac",
    "mcchicken",
    "whopper",
    "mcflurry",
}

CONDIMENT_KEYWORDS = {
    "sauce",
    "dressing",
    "dip",
    "seasoning",
    "marinade",
    "ketchup",
    "mustard",
    "mayonnaise",
    "gravy",
}


# Map classifier labels to USDA-friendly search phrases.
FOOD_CLASS_ALIASES = {
    "french_fries": "French fries",
    "fried_rice": "Fried rice",
    "ice_cream": "Ice cream",
    "macaroni_and_cheese": "Macaroni and cheese",
    "spaghetti_bolognese": "Spaghetti bolognese",
    "spaghetti_carbonara": "Spaghetti carbonara",
    "baby_back_ribs": "Pork ribs",
    "hot_dog": "Hot dog",
    "club_sandwich": "Club sandwich",
    "grilled_cheese_sandwich": "Grilled cheese sandwich",
    "lobster_roll_sandwich": "Lobster roll",
    "pulled_pork_sandwich": "Pulled pork sandwich",
}


def _normalize_food_name(food_class_name: str) -> str:
    clean = (food_class_name or "").strip().lower()
    if clean in FOOD_CLASS_ALIASES:
        return FOOD_CLASS_ALIASES[clean]
    return clean.replace("_", " ")


@lru_cache(maxsize=1)
def _known_food_queries() -> tuple[str, ...]:
    """
    Build a food phrase list for typo correction from model labels and aliases.
    """
    queries: set[str] = set()

    for alias in FOOD_CLASS_ALIASES.values():
        queries.add(alias.strip().lower())

    models_path = Path(__file__).resolve().parents[1] / "models" / "food_classes.txt"
    if models_path.exists():
        with models_path.open("r", encoding="utf-8") as f:
            for line in f:
                label = line.strip().lower()
                if not label:
                    continue
                queries.add(label.replace("_", " "))

    # Add common plain-language foods often used in text logs.
    queries.update(
        {
            "salmon",
            "chicken breast",
            "cheeseburger",
            "pepperoni pizza",
            "white rice",
            "brown rice",
            "fries",
            "french fries",
            "steak",
            "egg",
            "omelette",
            "banana",
            "apple",
            "yogurt",
        }
    )

    return tuple(sorted(queries))


@lru_cache(maxsize=1)
def _known_food_tokens() -> tuple[str, ...]:
    tokens: set[str] = set()
    for phrase in _known_food_queries():
        for token in phrase.split():
            if len(token) >= 3:
                tokens.add(token)
    return tuple(sorted(tokens))


def _maybe_correct_typo_query(query: str) -> str:
    """
    Correct likely typos in free-text food queries using conservative fuzzy matching.
    """
    cleaned = (query or "").strip().lower()
    if not cleaned:
        return cleaned

    # First try phrase-level fuzzy correction.
    phrase_match = get_close_matches(cleaned, _known_food_queries(), n=1, cutoff=TYPO_PHRASE_CUTOFF)
    if phrase_match:
        return phrase_match[0]

    # Then token-level correction for misspelled words.
    corrected_tokens = []
    changed = False
    for token in cleaned.split():
        mapped = COMMON_TYPO_MAP.get(token)
        if mapped:
            corrected_tokens.append(mapped)
            changed = changed or mapped != token
            continue

        if len(token) < 4 or not token.isalpha():
            corrected_tokens.append(token)
            continue

        token_match = get_close_matches(token, _known_food_tokens(), n=1, cutoff=TYPO_TOKEN_CUTOFF)
        if token_match:
            corrected_tokens.append(token_match[0])
            changed = changed or token_match[0] != token
        else:
            corrected_tokens.append(token)

    corrected = " ".join(corrected_tokens).strip()
    return corrected if changed else cleaned


def _extract_energy_kcal_per_100g(food: Dict) -> Optional[float]:
    nutrients = food.get("foodNutrients") or []
    for nutrient in nutrients:
        name = str(nutrient.get("nutrientName", "")).lower()
        unit = str(nutrient.get("unitName", "")).lower()
        if name in {"energy", "energy (kcal)"} and unit in {"kcal", "kcal_"}:
            value = nutrient.get("value")
            if value is not None:
                return float(value)

    # Some responses may use generic labels for energy in kcal.
    for nutrient in nutrients:
        name = str(nutrient.get("nutrientName", "")).lower()
        unit = str(nutrient.get("unitName", "")).lower()
        if "energy" in name and "kcal" in unit:
            value = nutrient.get("value")
            if value is not None:
                return float(value)

    return None


def _query_tokens(query: str) -> list[str]:
    raw = "".join(ch if (ch.isalnum() or ch.isspace()) else " " for ch in (query or "").lower())
    tokens = [t for t in raw.split() if len(t) >= 3]
    signal_tokens = [t for t in tokens if t not in LOW_SIGNAL_TOKENS]
    return signal_tokens or tokens


def _has_brand_intent(query: str) -> bool:
    q = (query or "").lower()
    return any(keyword in q for keyword in BRAND_INTENT_KEYWORDS)


def _normalize_query_text(query: str) -> str:
    raw = "".join(ch if (ch.isalnum() or ch.isspace()) else " " for ch in (query or "").lower())
    tokens = [t for t in raw.split() if t and t not in QUERY_NOISE_TOKENS]
    return " ".join(tokens).strip()


def _singularize_token(token: str) -> str:
    if len(token) <= 3:
        return token
    if token.endswith("ies") and len(token) > 4:
        return token[:-3] + "y"
    if token.endswith("es") and len(token) > 4:
        return token[:-2]
    if token.endswith("s") and len(token) > 3:
        return token[:-1]
    return token


def _generate_query_candidates(query: str) -> list[str]:
    base = (query or "").strip().lower()
    if not base:
        return []

    candidates: list[str] = []

    def add_candidate(value: str):
        v = value.strip().lower()
        if v and v not in candidates:
            candidates.append(v)

    normalized = _normalize_query_text(base)
    corrected = _maybe_correct_typo_query(base)
    corrected_normalized = _normalize_query_text(corrected)

    add_candidate(base)
    add_candidate(normalized)
    add_candidate(corrected)
    add_candidate(corrected_normalized)

    # Singularized fallback handles plural shorthand like "fries"/"tacos" variants.
    singularized = " ".join(_singularize_token(t) for t in normalized.split())
    corrected_singularized = " ".join(_singularize_token(t) for t in corrected_normalized.split())
    add_candidate(singularized)
    add_candidate(corrected_singularized)

    return candidates


def _score_food_match(query: str, food: Dict) -> float:
    description = str(food.get("description") or "").lower()
    data_type = str(food.get("dataType") or "")

    tokens = _query_tokens(query)
    if not tokens:
        token_ratio = 0.0
    else:
        token_hits = sum(1 for token in tokens if token in description)
        token_ratio = token_hits / len(tokens)

    data_type_score = PREFERRED_DATA_TYPE_SCORE.get(data_type, 1.0)
    brand_intent = _has_brand_intent(query)

    # For generic logs, penalize branded foods. For explicit brand logs, prefer them.
    if data_type == "Branded":
        branded_penalty = 1.5 if brand_intent else -2.0
    else:
        branded_penalty = 0.0

    # Keep USDA relevance when present but rely mostly on token overlap and data type.
    usda_score = float(food.get("score") or 0.0)

    query_tokens = set(tokens)
    context_penalty = 0.0

    if "rice" in query_tokens and "noodle" in description and "noodle" not in query_tokens:
        context_penalty -= 3.0

    if "beef" in query_tokens and any(bad in description for bad in ("snack stick", "jerky", "sausage")):
        context_penalty -= 3.0

    if any(brand in description for brand in CHAIN_BRAND_KEYWORDS) and not brand_intent:
        context_penalty -= 2.5

    if any(k in description for k in CONDIMENT_KEYWORDS) and not any(k in query_tokens for k in CONDIMENT_KEYWORDS):
        context_penalty -= 4.0

    return (token_ratio * 10.0) + data_type_score + (usda_score * 0.01) + branded_penalty + context_penalty


def _search_usda_foods(query: str) -> Dict:
    api_key = os.getenv("USDA_API_KEY")

    if not api_key:
        return {
            "ok": False,
            "error": {
                "found": False,
                "source": "none",
                "reason": "missing_usda_api_key",
                "query": query,
            },
            "foods": [],
        }

    params = urlencode(
        {
            "query": query,
            "pageSize": 10,
            "api_key": api_key,
        }
    )
    url = f"{USDA_SEARCH_URL}?{params}"

    try:
        with urlopen(url, timeout=5) as response:
            payload = json.loads(response.read().decode("utf-8"))
    except (HTTPError, URLError, TimeoutError, ValueError):
        return {
            "ok": False,
            "error": {
                "found": False,
                "source": "usda_fdc",
                "reason": "request_failed",
                "query": query,
            },
            "foods": [],
        }

    return {
        "ok": True,
        "error": None,
        "foods": payload.get("foods") or [],
    }


def _best_kcal_match_from_foods(query: str, foods: list[Dict]) -> Dict:
    candidates = []
    tokens = _query_tokens(query)

    for food in foods:
        kcal_per_100g = _extract_energy_kcal_per_100g(food)
        if kcal_per_100g is None:
            continue

        description = str(food.get("description") or "").lower()

        # If we have meaningful tokens, require at least one to appear.
        if tokens and not any(token in description for token in tokens):
            continue

        candidates.append((food, kcal_per_100g, _score_food_match(query, food)))

    if not candidates:
        return {
            "found": False,
            "source": "usda_fdc",
            "reason": "no_food_match",
            "query": query,
        }

    best_food, best_kcal_100g, _ = max(candidates, key=lambda entry: entry[2])
    best_score = max(candidates, key=lambda entry: entry[2])[2]
    return {
        "found": True,
        "source": "usda_fdc",
        "query": query,
        "matched_description": best_food.get("description"),
        "fdc_id": best_food.get("fdcId"),
        "data_type": best_food.get("dataType"),
        "match_score": round(float(best_score), 4),
        "kcal_per_100g": round(best_kcal_100g, 2),
        "kcal_per_gram": round(best_kcal_100g / 100.0, 4),
    }


def _lookup_kcal_per_gram_by_query(query: str) -> Dict:
    search = _search_usda_foods(query)
    if not search["ok"]:
        return search["error"]
    return _best_kcal_match_from_foods(query, search["foods"])


@lru_cache(maxsize=256)
def lookup_usda_kcal_per_gram(food_class_name: str) -> Dict:
    """
    Query USDA FoodData Central and return kcal/gram when available.
    """
    query = _normalize_food_name(food_class_name)
    return _lookup_kcal_per_gram_by_query(query)


@lru_cache(maxsize=256)
def lookup_usda_kcal_per_gram_query(food_query: str) -> Dict:
    """
    Query USDA FoodData Central using free-form food text.
    """
    query = (food_query or "").strip().lower()
    if not query:
        return {
            "found": False,
            "source": "none",
            "reason": "empty_query",
            "query": query,
        }

    queries_to_try = _generate_query_candidates(query)
    best_found: Dict | None = None
    best_score = float("-inf")

    for candidate_query in queries_to_try:
        candidate_result = _lookup_kcal_per_gram_by_query(candidate_query)
        if not candidate_result.get("found"):
            continue

        candidate_score = float(candidate_result.get("match_score") or 0.0)
        if candidate_score > best_score:
            best_found = candidate_result
            best_score = candidate_score

    if best_found is not None:
        if str(best_found.get("query")) != query:
            best_found["original_query"] = query
            best_found["resolved_query"] = best_found.get("query")
        best_found["query_candidates_tried"] = len(queries_to_try)
        return best_found

    # Keep previous behavior for non-matching queries.
    return _lookup_kcal_per_gram_by_query(query)

import json
import os
from concurrent.futures import ThreadPoolExecutor, as_completed
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

TAVILY_API_URL = "https://api.tavily.com/search"


def _search_one(food_name: str, api_key: str) -> dict:
    """Run one Tavily search for a single food item. Returns a result dict."""
    query = f"{food_name} nutrition facts calories protein carbs fat per 100g"
    body = {
        "api_key": api_key,
        "query": query,
        "search_depth": "basic",
        "max_results": 3,
        "include_answer": True,
        "include_raw_content": False,
    }
    req = Request(
        TAVILY_API_URL,
        data=json.dumps(body).encode("utf-8"),
        headers={"Content-Type": "application/json", "User-Agent": "CalorAI/1.0"},
        method="POST",
    )
    try:
        with urlopen(req, timeout=8) as resp:
            payload = json.loads(resp.read().decode("utf-8"))
        results = payload.get("results", [])
        answer = payload.get("answer") or ""
        sources = [
            {"title": r.get("title", ""), "url": r.get("url", ""), "snippet": r.get("content", "")[:400]}
            for r in results
        ]
        return {"food": food_name, "answer": answer, "sources": sources, "ok": True}
    except (HTTPError, URLError, TimeoutError, ValueError, Exception) as exc:
        return {"food": food_name, "answer": "", "sources": [], "ok": False, "error": str(exc)}


def search_nutrition_for_foods(food_names: list[str]) -> list[dict]:
    """
    Run Tavily nutrition searches for a list of food names in parallel.
    Silently skips if TAVILY_API_KEY is not set.
    Returns list of result dicts ordered by input food_names.
    """
    api_key = os.getenv("TAVILY_API_KEY", "").strip()
    if not api_key or not food_names:
        return []

    results_by_food: dict[str, dict] = {}
    with ThreadPoolExecutor(max_workers=min(len(food_names), 5)) as pool:
        futures = {pool.submit(_search_one, name, api_key): name for name in food_names}
        for future in as_completed(futures):
            result = future.result()
            results_by_food[result["food"]] = result

    return [results_by_food.get(name, {"food": name, "answer": "", "sources": [], "ok": False}) for name in food_names]

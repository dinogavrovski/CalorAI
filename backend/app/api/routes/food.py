import json
from urllib.request import urlopen, Request
from urllib.error import HTTPError, URLError

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel

router = APIRouter(prefix="/food", tags=["food"])


class BarcodeProduct(BaseModel):
    barcode: str
    name: str
    brand: str | None = None
    serving_size_g: float
    serving_description: str | None = None
    calories_per_serving: float
    protein_g: float
    carbs_g: float
    fat_g: float
    image_url: str | None = None


def _fetch_off(barcode: str) -> dict:
    """Fetch product from Open Food Facts."""
    url = f"https://world.openfoodfacts.org/api/v0/product/{barcode}.json"
    req = Request(url, headers={"User-Agent": "CalorAI/1.0 (contact@calorai.app)"})
    try:
        with urlopen(req, timeout=8) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except (HTTPError, URLError, ValueError) as exc:
        raise HTTPException(status_code=502, detail=f"Open Food Facts error: {exc}")


def _safe_float(val, default: float = 0.0) -> float:
    try:
        return float(val or default)
    except (TypeError, ValueError):
        return default


@router.get("/barcode/{barcode}", response_model=BarcodeProduct)
def get_barcode_product(barcode: str):
    data = _fetch_off(barcode)

    if data.get("status") != 1:
        raise HTTPException(status_code=404, detail="Product not found in Open Food Facts database")

    p = data.get("product", {})
    nutriments = p.get("nutriments", {})

    # Serving size — prefer product_quantity, fall back to serving_size string
    serving_g = _safe_float(p.get("product_quantity") or p.get("serving_quantity"), 100.0)
    if serving_g <= 0:
        serving_g = 100.0

    # Nutrients are stored per 100g in OFF; scale to serving size
    factor = serving_g / 100.0
    calories = _safe_float(nutriments.get("energy-kcal_100g") or nutriments.get("energy-kcal")) * factor
    protein  = _safe_float(nutriments.get("proteins_100g") or nutriments.get("proteins")) * factor
    carbs    = _safe_float(nutriments.get("carbohydrates_100g") or nutriments.get("carbohydrates")) * factor
    fat      = _safe_float(nutriments.get("fat_100g") or nutriments.get("fat")) * factor

    name = (
        p.get("product_name_en")
        or p.get("product_name")
        or p.get("generic_name_en")
        or p.get("generic_name")
        or "Unknown Product"
    ).strip()

    brand = (p.get("brands") or "").split(",")[0].strip() or None
    image = p.get("image_front_small_url") or p.get("image_url") or None
    serving_desc = p.get("serving_size") or f"{serving_g:.0f}g"

    return BarcodeProduct(
        barcode=barcode,
        name=name,
        brand=brand,
        serving_size_g=round(serving_g, 1),
        serving_description=serving_desc,
        calories_per_serving=round(calories, 1),
        protein_g=round(protein, 1),
        carbs_g=round(carbs, 1),
        fat_g=round(fat, 1),
        image_url=image,
    )

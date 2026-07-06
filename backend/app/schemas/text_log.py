from pydantic import BaseModel, Field


class TextLogRequest(BaseModel):
    note: str = Field(..., min_length=2, max_length=300)


class BarcodeLogItem(BaseModel):
    """An exact, pre-computed item from a barcode scan — bypasses AI estimation."""
    note: str
    calories: float
    protein_g: float = 0.0
    carbs_g: float = 0.0
    fat_g: float = 0.0


class MealLogRequest(BaseModel):
    """A meal to log: an optional typed note (AI-estimated) plus any scanned barcode items (exact)."""
    note: str = Field(default="", max_length=300)
    barcode_items: list[BarcodeLogItem] = []


class TextLogItem(BaseModel):
    note_part: str
    parsed_food: str
    quantity: float
    unit: str | None
    estimated_grams: float
    kcal_per_gram: float
    calories: float
    calorie_range: list[float]
    protein_g: float = 0.0
    carbs_g: float = 0.0
    fat_g: float = 0.0
    nutrition_source: str
    source_label: str | None = None
    matched_description: str | None


class TextLogResponse(BaseModel):
    note: str
    items: list[TextLogItem]
    total_calories: float
    total_calorie_range: list[float]
    total_protein_g: float = 0.0
    total_carbs_g: float = 0.0
    total_fat_g: float = 0.0
    sources: list[str] = []
    web_grounded: bool = False

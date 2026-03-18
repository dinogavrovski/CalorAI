from pydantic import BaseModel, Field


class TextLogRequest(BaseModel):
    note: str = Field(..., min_length=2, max_length=300)


class TextLogItem(BaseModel):
    note_part: str
    parsed_food: str
    quantity: float
    unit: str | None
    estimated_grams: float
    kcal_per_gram: float
    calories: float
    calorie_range: list[float]
    nutrition_source: str
    matched_description: str | None


class TextLogResponse(BaseModel):
    note: str
    items: list[TextLogItem]
    total_calories: float
    total_calorie_range: list[float]

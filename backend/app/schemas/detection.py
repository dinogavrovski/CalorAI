from pydantic import BaseModel
from typing import List


class DetectedFood(BaseModel):
    class_name: str
    mask_pixels: int
    confidence: float


class DetectionPayload(BaseModel):
    total_plate_pixels: int
    items: List[DetectedFood]

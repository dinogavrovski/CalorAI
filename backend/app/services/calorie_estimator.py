from typing import Dict
from app.schemas.detection import DetectionPayload

CALORIE_DENSITY = {
    "rice": 1.3,
    "chicken": 1.65,
    "broccoli": 0.35,
    "beef": 2.5,
    "pasta": 1.5,
}

AVERAGE_TOTAL_PLATE_WEIGHT = 500  # grams


def estimate_calories(payload: DetectionPayload) -> Dict:

    results = []
    total_kcal = 0
    total_kcal_low = 0
    total_kcal_high = 0

    for item in payload.items:
        area_ratio = item.mask_pixels / payload.total_plate_pixels

        estimated_grams = area_ratio * AVERAGE_TOTAL_PLATE_WEIGHT

        kcal_per_gram = CALORIE_DENSITY.get(item.class_name, 1.5)
        estimated_kcal = estimated_grams * kcal_per_gram

        lower_bound = estimated_kcal * 0.85
        upper_bound = estimated_kcal * 1.15

        results.append({
            "name": item.class_name,
            "confidence": round(item.confidence, 2),
            "estimated_grams": round(estimated_grams, 1),
            "calories": round(estimated_kcal, 1),
            "calorie_range": [
                round(lower_bound, 1),
                round(upper_bound, 1)
            ]
        })

        total_kcal += estimated_kcal
        total_kcal_low += lower_bound
        total_kcal_high += upper_bound

    return {
        "items": results,
        "total_calories": round(total_kcal, 1),
        "total_calorie_range": [
            round(total_kcal_low, 1),
            round(total_kcal_high, 1)
        ]
    }

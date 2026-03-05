from app.schemas.detection import DetectionPayload


def run_inference(image_bytes: bytes) -> DetectionPayload:
    """
    This will later run YOLO.
    For now, returns fake data.
    """

    return DetectionPayload(
        total_plate_pixels=12000,
        items=[
            {
                "class_name": "rice",
                "mask_pixels": 7000,
                "confidence": 0.91
            },
            {
                "class_name": "chicken",
                "mask_pixels": 5000,
                "confidence": 0.87
            }
        ]
    )

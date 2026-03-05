from ultralytics import YOLO
import numpy as np
from PIL import Image
from io import BytesIO
from typing import List, Dict

# Load YOLOv8 model
MODEL_PATH = "yolov8n-seg.pt"
model = YOLO(MODEL_PATH)

def analyze_image(image_bytes: bytes) -> List[Dict]:
    """
    Input: image bytes (from FastAPI UploadFile)
    Output: List of detected items with mask pixels
    """
    image = np.array(Image.open(BytesIO(image_bytes)).convert("RGB"))
    results = model.predict(image)

    output = []
    for result in results:
        if result.masks is not None:
            masks = result.masks.data
            classes = result.boxes.cls

            for i, cls_id in enumerate(classes):
                # Get the human-readable label from YOLO
                class_name = result.names[int(cls_id)]
                mask = masks[i].cpu().numpy()
                mask_pixels = int(np.sum(mask))
                output.append({"name": class_name, "mask_pixels": mask_pixels})


    return output

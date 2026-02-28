import io
from PIL import Image
import torch
from transformers import AutoImageProcessor, AutoModelForImageClassification

class FoodModel:
    def __init__(self):
        self.device = "cuda" if torch.cuda.is_available() else "cpu"

        # Load a real pretrained Food‑101 classifier
        self.model_name = "AventIQ-AI/Food-Classification-AI-Model"
        self.processor = AutoImageProcessor.from_pretrained(self.model_name)
        self.model = AutoModelForImageClassification.from_pretrained(self.model_name)
        self.model.to(self.device)
        self.model.eval()

        # id2label mapping
        self.class_names = self.model.config.id2label

    def predict(self, image_bytes: bytes):
        image = Image.open(io.BytesIO(image_bytes)).convert("RGB")

        # Preprocess
        inputs = self.processor(images=image, return_tensors="pt")
        pixel_values = inputs["pixel_values"].to(self.device)

        with torch.no_grad():
            outputs = self.model(pixel_values=pixel_values)
            logits = outputs.logits

        probs = torch.nn.functional.softmax(logits[0], dim=0)

        # Pick the highest probability
        confidence, predicted_idx = torch.max(probs, dim=0)
        label = self.class_names[predicted_idx.item()]

        return {
            "label": label,
            "confidence": float(confidence.item())
        }

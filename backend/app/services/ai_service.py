from app.models.food_model import FoodModel

class AIService:

    def __init__(self):
        self.model = FoodModel()
        self.calories_lookup = {
            "pizza": 530,
            "burger": 650,
            "sushi": 300,
            "ramen": 450,
            "steak": 700
        }

    def analyze(self, image_bytes: bytes):
        prediction = self.model.predict(image_bytes)
        calories = self.calories_lookup.get(prediction["label"].lower(), 250)
        return {
            "food": prediction["label"],
            "confidence": prediction["confidence"],
            "estimated_calories": calories
        }

from fastapi import FastAPI
from app.api.routes import analyze

app = FastAPI(title="AI Calorie API")

app.include_router(analyze.router)

@app.get("/")
def root():
    return {"message": "CalorAI is running! 🚀"}

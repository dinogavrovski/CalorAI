from pathlib import Path

from dotenv import load_dotenv
from fastapi import FastAPI
from app.api.routes import ai, auth, user as user_router
from app.db.database import engine
from app.db.base import Base


PROJECT_ROOT = Path(__file__).resolve().parents[2]
load_dotenv(dotenv_path=PROJECT_ROOT / ".env")

Base.metadata.create_all(bind=engine)

app = FastAPI(title="AI Calorie API")

app.include_router(auth.router)
app.include_router(user_router.router)
app.include_router(ai.router)

@app.get("/")
def root():
    return {"message": "CalorAI is running! 🚀"}

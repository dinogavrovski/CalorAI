from fastapi import FastAPI
from app.api.routes import analyze
from fastapi import FastAPI
from app.api.routes import analyze, auth, user as user_router
from app.db.database import engine
from app.db.base import Base
from app.models import user


Base.metadata.create_all(bind=engine)

app = FastAPI(title="AI Calorie API")

app.include_router(auth.router)
app.include_router(user_router.router)
app.include_router(analyze.router)

@app.get("/")
def root():
    return {"message": "CalorAI is running! 🚀"}

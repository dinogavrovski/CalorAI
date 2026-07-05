from pathlib import Path
import os

from dotenv import load_dotenv
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.openapi.utils import get_openapi
from sqlalchemy import text
from app.api.routes import ai, auth, user as user_router
from app.api.routes import weight as weight_router
from app.api.routes import food as food_router
from app.db.database import engine
from app.db.base import Base
from app.models import meal_log, refresh_session, saved_meal, user, weight_log  # noqa: F401

PROJECT_ROOT = Path(__file__).resolve().parents[1]
load_dotenv(dotenv_path=PROJECT_ROOT / ".env")

Base.metadata.create_all(bind=engine)

# Add biometric columns to existing DBs that predate this migration
_NEW_COLUMNS = [
    ("height_cm", "REAL"),
    ("age", "INTEGER"),
    ("sex", "TEXT"),
    ("current_weight_kg", "REAL"),
    ("goal_weight_kg", "REAL"),
    ("weekly_goal_kg", "REAL"),
    ("activity_level", "TEXT"),
    ("calorie_goal", "INTEGER DEFAULT 2000"),
]
with engine.connect() as _conn:
    for _col, _type in _NEW_COLUMNS:
        try:
            _conn.execute(text(f"ALTER TABLE users ADD COLUMN {_col} {_type}"))
            _conn.commit()
        except Exception:
            pass  # column already exists

app = FastAPI(title="AI Calorie API")

# Allow frontend preflight requests (OPTIONS) and authenticated API calls.
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:8081",
        "http://localhost:8083",
        "http://localhost:19006",
        "http://127.0.0.1:8081",
        "http://127.0.0.1:8083",
        "http://127.0.0.1:19006",
    ],
    allow_origin_regex=r"https?://(localhost|127\.0\.0\.1)(:\d+)?$",
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth.router)
app.include_router(user_router.router)
app.include_router(ai.router)
app.include_router(weight_router.router)
app.include_router(food_router.router)


def _visible_swagger_paths() -> set[str]:
    raw_paths = os.getenv(
        "SWAGGER_VISIBLE_PATHS",
        "/ai/log-text,/user/meal-history,/user/meal-history/{meal_id}",
    )
    return {path.strip() for path in raw_paths.split(",") if path.strip()}


def custom_openapi():
    if app.openapi_schema:
        return app.openapi_schema

    schema = get_openapi(
        title=app.title,
        version="1.0.0",
        description="AI-first calorie logging API",
        routes=app.routes,
    )

    allowed_paths = _visible_swagger_paths()
    schema["paths"] = {
        path: methods
        for path, methods in schema.get("paths", {}).items()
        if path in allowed_paths
    }

    app.openapi_schema = schema
    return app.openapi_schema


app.openapi = custom_openapi

@app.get("/")
def root():
    return {"message": "CalorAI is running! 🚀"}


@app.get("/health/db")
def db_health():
    try:
        with engine.connect() as connection:
            connection.execute(text("SELECT 1"))
        return {"ok": True, "database": "reachable"}
    except Exception as exc:
        return {"ok": False, "database": "unreachable", "detail": str(exc)}

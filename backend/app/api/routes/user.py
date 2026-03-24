from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.db.database import get_db
from app.dependencies.auth import get_current_user
from app.models.meal_log import MealLog
from app.schemas.text_log import TextLogResponse
from app.schemas.user import UserResponse


router = APIRouter(prefix="/user", tags=["User"])


def _serialize_meal_log(log: MealLog) -> dict:
    return {
        "id": log.id,
        "user_id": log.user_id,
        "note": log.note,
        "items": log.items_json,
        "total_calories": round(float(log.total_calories), 1),
        "total_calorie_range": [
            round(float(log.total_calorie_low), 1),
            round(float(log.total_calorie_high), 1),
        ],
        "timestamp": log.created_at.isoformat(),
    }


def _dump_text_log_item(item) -> dict:
    if hasattr(item, "model_dump"):
        return item.model_dump()
    return item.dict()


@router.get("/me", response_model=UserResponse)
def me(current_user=Depends(get_current_user)):
    return current_user


@router.post("/meal-history")
def save_meal_history(
    payload: TextLogResponse,
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db),
):
    low, high = payload.total_calorie_range

    meal_log = MealLog(
        user_id=current_user.id,
        note=payload.note,
        items_json=[_dump_text_log_item(item) for item in payload.items],
        total_calories=float(payload.total_calories),
        total_calorie_low=float(low),
        total_calorie_high=float(high),
    )
    db.add(meal_log)
    db.commit()
    db.refresh(meal_log)

    return _serialize_meal_log(meal_log)


@router.get("/meal-history")
def get_meal_history(
    limit: int = Query(default=30, ge=1, le=200),
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db),
):
    logs = (
        db.query(MealLog)
        .filter(MealLog.user_id == current_user.id)
        .order_by(MealLog.created_at.desc())
        .limit(limit)
        .all()
    )

    return [_serialize_meal_log(log) for log in logs]

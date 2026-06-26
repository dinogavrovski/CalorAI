from fastapi import APIRouter, Depends, HTTPException, Query
from pydantic import BaseModel
from sqlalchemy.orm import Session

from app.db.database import get_db
from app.dependencies.auth import get_current_user
from app.models.meal_log import MealLog
from app.models.saved_meal import SavedMeal
from app.schemas.text_log import TextLogRequest
from app.schemas.user import UserResponse
from app.services.text_calorie_estimator import estimate_from_text_note


class SavedMealRequest(BaseModel):
    name: str
    calories: float


class QuickLogRequest(BaseModel):
    name: str
    calories: float


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


@router.get("/me", response_model=UserResponse)
def me(current_user=Depends(get_current_user)):
    return current_user


@router.post("/meal-history")
def save_meal_history(
    payload: TextLogRequest,
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db),
):
    # Never trust client-provided nutrition totals; recompute on the server.
    try:
        estimate = estimate_from_text_note(payload.note)
    except RuntimeError as exc:
        raise HTTPException(status_code=502, detail=str(exc)) from exc
    low, high = estimate["total_calorie_range"]

    meal_log = MealLog(
        user_id=current_user.id,
        note=estimate["note"],
        items_json=estimate["items"],
        total_calories=float(estimate["total_calories"]),
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


@router.patch("/meal-history/{meal_id}")
def patch_meal_calories(
    meal_id: int,
    payload: dict,
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db),
):
    meal_log = (
        db.query(MealLog)
        .filter(MealLog.id == meal_id, MealLog.user_id == current_user.id)
        .first()
    )
    if meal_log is None:
        raise HTTPException(status_code=404, detail="Meal not found")

    new_calories = payload.get("total_calories")
    if new_calories is None or not isinstance(new_calories, (int, float)) or new_calories <= 0:
        raise HTTPException(status_code=422, detail="total_calories must be a positive number")

    meal_log.total_calories = float(new_calories)
    meal_log.total_calorie_low = float(new_calories)
    meal_log.total_calorie_high = float(new_calories)

    db.commit()
    db.refresh(meal_log)

    return _serialize_meal_log(meal_log)


@router.put("/meal-history/{meal_id}")
def update_meal_history(
    meal_id: int,
    payload: TextLogRequest,
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db),
):
    meal_log = (
        db.query(MealLog)
        .filter(MealLog.id == meal_id, MealLog.user_id == current_user.id)
        .first()
    )

    if meal_log is None:
        raise HTTPException(status_code=404, detail="Meal not found")

    # Never trust client-side nutrition values when editing; recompute on server.
    try:
        estimate = estimate_from_text_note(payload.note)
    except RuntimeError as exc:
        raise HTTPException(status_code=502, detail=str(exc)) from exc
    low, high = estimate["total_calorie_range"]

    meal_log.note = estimate["note"]
    meal_log.items_json = estimate["items"]
    meal_log.total_calories = float(estimate["total_calories"])
    meal_log.total_calorie_low = float(low)
    meal_log.total_calorie_high = float(high)

    db.commit()
    db.refresh(meal_log)

    return _serialize_meal_log(meal_log)


# ── Quick log (from saved meal, no AI re-run) ────────────────────────────────

@router.post("/meal-history/quick")
def quick_log_meal(
    payload: QuickLogRequest,
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db),
):
    if payload.calories <= 0:
        raise HTTPException(status_code=422, detail="calories must be positive")
    meal_log = MealLog(
        user_id=current_user.id,
        note=payload.name,
        items_json=[],
        total_calories=float(payload.calories),
        total_calorie_low=float(payload.calories),
        total_calorie_high=float(payload.calories),
    )
    db.add(meal_log)
    db.commit()
    db.refresh(meal_log)
    return _serialize_meal_log(meal_log)


# ── Saved meals CRUD ─────────────────────────────────────────────────────────

def _serialize_saved_meal(s: SavedMeal) -> dict:
    return {"id": s.id, "name": s.name, "calories": round(float(s.calories), 1)}


@router.get("/saved-meals")
def get_saved_meals(
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db),
):
    meals = (
        db.query(SavedMeal)
        .filter(SavedMeal.user_id == current_user.id)
        .order_by(SavedMeal.created_at.desc())
        .all()
    )
    return [_serialize_saved_meal(m) for m in meals]


@router.post("/saved-meals")
def create_saved_meal(
    payload: SavedMealRequest,
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db),
):
    if payload.calories <= 0:
        raise HTTPException(status_code=422, detail="calories must be positive")
    saved = SavedMeal(
        user_id=current_user.id,
        name=payload.name,
        calories=float(payload.calories),
    )
    db.add(saved)
    db.commit()
    db.refresh(saved)
    return _serialize_saved_meal(saved)


@router.delete("/saved-meals/{saved_meal_id}")
def delete_saved_meal(
    saved_meal_id: int,
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db),
):
    saved = (
        db.query(SavedMeal)
        .filter(SavedMeal.id == saved_meal_id, SavedMeal.user_id == current_user.id)
        .first()
    )
    if saved is None:
        raise HTTPException(status_code=404, detail="Saved meal not found")
    db.delete(saved)
    db.commit()
    return {"ok": True}

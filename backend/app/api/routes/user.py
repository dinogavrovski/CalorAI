from fastapi import APIRouter, Depends, HTTPException, Query
from pydantic import BaseModel
from sqlalchemy.orm import Session

from app.db.database import get_db
from app.dependencies.auth import get_current_user
from app.models.meal_log import MealLog
from app.models.saved_meal import SavedMeal
from app.models.user import User
from app.schemas.text_log import TextLogRequest, MealLogRequest
from app.schemas.user import UserResponse, UpdateProfileRequest
from app.services.text_calorie_estimator import estimate_from_text_note
from app.services.tdee import calculate_tdee


class SavedMealRequest(BaseModel):
    name: str
    calories: float


class QuickLogRequest(BaseModel):
    name: str
    calories: float


class BarcodeLogRequest(BaseModel):
    note: str
    total_calories: float
    total_protein_g: float = 0.0
    total_carbs_g: float = 0.0
    total_fat_g: float = 0.0


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


@router.get("/profile", response_model=UserResponse)
def get_profile(current_user=Depends(get_current_user)):
    return current_user


@router.patch("/profile", response_model=UserResponse)
def update_profile(
    payload: UpdateProfileRequest,
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db),
):
    # Re-fetch within this session so setattr + commit are on the same session
    user = db.query(User).filter(User.id == current_user.id).first()
    if user is None:
        raise HTTPException(status_code=404, detail="User not found")

    fields = ["display_name", "height_cm", "age", "sex", "current_weight_kg", "goal_weight_kg", "weekly_goal_kg", "activity_level"]
    for field in fields:
        value = getattr(payload, field)
        if value is not None:
            setattr(user, field, value)

    # Auto-calculate calorie goal if all biometrics are present
    if all(getattr(user, f) is not None for f in ["current_weight_kg", "height_cm", "age", "sex", "activity_level"]):
        user.calorie_goal = calculate_tdee(
            weight_kg=user.current_weight_kg,
            height_cm=user.height_cm,
            age=user.age,
            sex=user.sex,
            activity_level=user.activity_level,
            weekly_goal_kg=user.weekly_goal_kg or 0.0,
        )
    elif payload.calorie_goal is not None:
        user.calorie_goal = payload.calorie_goal

    db.commit()
    db.refresh(user)
    return user


@router.post("/meal-history")
def save_meal_history(
    payload: MealLogRequest,
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db),
):
    items: list[dict] = []
    total_cal = 0.0
    total_low = 0.0
    total_high = 0.0

    # ── Typed portion: AI-estimated (never trust client totals; recompute here) ──
    note_text = payload.note.strip()
    if len(note_text) >= 2:
        try:
            estimate = estimate_from_text_note(note_text)
        except RuntimeError as exc:
            raise HTTPException(status_code=502, detail=str(exc)) from exc
        low, high = estimate["total_calorie_range"]
        items.extend(estimate["items"])
        total_cal += float(estimate["total_calories"])
        total_low += float(low)
        total_high += float(high)

    # ── Scanned portion: exact barcode nutrition, no AI ─────────────────────────
    for b in payload.barcode_items:
        if b.calories <= 0:
            continue
        items.append({
            "note_part": b.note,
            "parsed_food": b.note,
            "calories": round(b.calories, 1),
            "calorie_range": [round(b.calories * 0.98, 1), round(b.calories * 1.02, 1)],
            "protein_g": round(b.protein_g, 1),
            "carbs_g": round(b.carbs_g, 1),
            "fat_g": round(b.fat_g, 1),
            "nutrition_source": "barcode",
        })
        total_cal += b.calories
        total_low += b.calories * 0.98
        total_high += b.calories * 1.02

    if not items:
        raise HTTPException(
            status_code=422,
            detail="Meal must contain a description or at least one scanned item",
        )

    # Combined note for display in history
    barcode_names = ", ".join(b.note for b in payload.barcode_items if b.calories > 0)
    combined_note = ", ".join(part for part in (note_text, barcode_names) if part)

    meal_log = MealLog(
        user_id=current_user.id,
        note=combined_note,
        items_json=items,
        total_calories=round(total_cal, 1),
        total_calorie_low=round(total_low, 1),
        total_calorie_high=round(total_high, 1),
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


@router.post("/meal-history/barcode")
def barcode_log_meal(
    payload: BarcodeLogRequest,
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """Log a meal from barcode scan — skips AI estimation, uses provided nutrition data."""
    if payload.total_calories <= 0:
        raise HTTPException(status_code=422, detail="total_calories must be positive")
    item = {
        "note_part": payload.note,
        "parsed_food": payload.note,
        "calories": payload.total_calories,
        "calorie_range": [payload.total_calories * 0.95, payload.total_calories * 1.05],
        "protein_g": payload.total_protein_g,
        "carbs_g": payload.total_carbs_g,
        "fat_g": payload.total_fat_g,
        "nutrition_source": "barcode",
    }
    meal_log = MealLog(
        user_id=current_user.id,
        note=payload.note,
        items_json=[item],
        total_calories=float(payload.total_calories),
        total_calorie_low=float(payload.total_calories * 0.95),
        total_calorie_high=float(payload.total_calories * 1.05),
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

from fastapi import APIRouter, Depends, HTTPException
from app.dependencies.auth import get_current_user
from app.schemas.text_log import TextLogRequest, TextLogResponse
from app.services.text_calorie_estimator import estimate_from_text_note


router = APIRouter(prefix="/ai", tags=["AI"])


@router.post("/log-text", response_model=TextLogResponse)
async def log_food_text(
    payload: TextLogRequest,
    current_user=Depends(get_current_user),
):
    try:
        return estimate_from_text_note(payload.note)
    except RuntimeError as exc:
        raise HTTPException(status_code=502, detail=str(exc)) from exc


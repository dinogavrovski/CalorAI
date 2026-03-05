from fastapi import APIRouter, UploadFile, File, Depends
from app.schemas.detection import DetectionPayload
from sqlalchemy.orm import Session
from app.db.database import get_db
from app.services.calorie_estimator import estimate_calories
from app.dependencies.auth import get_current_user
from app.services.model_inference import run_inference


router = APIRouter(prefix="/ai", tags=["AI"])


@router.post("/analyze-meal")
async def analyze_meal(file: UploadFile = File(...), current_user=Depends(get_current_user), db: Session = Depends(get_db)):
    image_bytes = await file.read()

    detection_payload = run_inference(image_bytes)
    result = estimate_calories(detection_payload)

    return result


from fastapi import APIRouter, UploadFile, File
from app.services.ai_service import AIService

router = APIRouter(prefix="/ai", tags=["AI"])

ai_service = AIService()

@router.post("/analyze-image")
async def analyze_image(file: UploadFile = File(...)):
    contents = await file.read()
    result = ai_service.analyze(contents)
    return result

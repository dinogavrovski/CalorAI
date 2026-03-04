from fastapi import APIRouter, Depends, UploadFile, File
from app.services.ai_service import AIService
from app.dependencies.auth import get_current_user

router = APIRouter(prefix="/ai", tags=["AI"])

ai_service = AIService()

@router.post("/analyze-image")
async def analyze_image(file: UploadFile = File(...), current_user=Depends(get_current_user)):
    contents = await file.read()
    result = ai_service.analyze(contents)
    print("Authenticated user ID:", current_user.id)
    return result

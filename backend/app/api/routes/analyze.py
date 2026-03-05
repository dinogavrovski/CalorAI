from fastapi import APIRouter, UploadFile, File
from app.services.yolo_service import analyze_image  # your YOLO function

router = APIRouter(prefix="/ai", tags=["AI"])

@router.post("/analyze")
async def analyze(file: UploadFile = File(...)):
    """
    Public / testing endpoint: returns YOLO detection results
    """
    image_bytes = await file.read()
    result = analyze_image(image_bytes)
    return {"items": result}

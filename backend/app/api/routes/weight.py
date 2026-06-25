from datetime import datetime, timedelta
from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from pydantic import BaseModel, Field

from app.db.database import get_db
from app.dependencies.auth import get_current_user
from app.models.weight_log import WeightLog

router = APIRouter(prefix="/user/weight", tags=["Weight"])


class WeightRequest(BaseModel):
    weight_kg: float = Field(..., gt=0, lt=500)
    logged_at: datetime | None = None  # optional override, defaults to now


def _serialize(log: WeightLog) -> dict:
    return {
        "id": log.id,
        "weight_kg": round(log.weight_kg, 2),
        "logged_at": log.logged_at.isoformat(),
    }


@router.post("")
def log_weight(
    payload: WeightRequest,
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db),
):
    entry = WeightLog(
        user_id=current_user.id,
        weight_kg=payload.weight_kg,
        logged_at=payload.logged_at or datetime.utcnow(),
    )
    db.add(entry)
    db.commit()
    db.refresh(entry)
    return _serialize(entry)


@router.get("")
def get_weight_history(
    period: str = Query(default="month", pattern="^(week|month|year)$"),
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db),
):
    now = datetime.utcnow()
    cutoffs = {"week": now - timedelta(days=7), "month": now - timedelta(days=30), "year": now - timedelta(days=365)}
    since = cutoffs[period]

    logs = (
        db.query(WeightLog)
        .filter(WeightLog.user_id == current_user.id, WeightLog.logged_at >= since)
        .order_by(WeightLog.logged_at.asc())
        .all()
    )
    return [_serialize(log) for log in logs]


@router.get("/latest")
def get_latest_weight(
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db),
):
    log = (
        db.query(WeightLog)
        .filter(WeightLog.user_id == current_user.id)
        .order_by(WeightLog.logged_at.desc())
        .first()
    )
    return _serialize(log) if log else None

from dataclasses import dataclass
from datetime import datetime, timezone
import os

from sqlalchemy.orm import Session

from app.models.ai_usage_window import AiUsageWindow
from app.models.user import User

AI_REQUESTS_PER_MINUTE = int(os.getenv("AI_REQUESTS_PER_MINUTE", 3))
AI_REQUESTS_PER_DAY = int(os.getenv("AI_REQUESTS_PER_DAY", 25))


@dataclass
class QuotaStatus:
    minute_used: int
    minute_limit: int
    day_used: int
    day_limit: int


class QuotaExceededError(RuntimeError):
    pass


def _window_start(now: datetime, window_type: str) -> datetime:
    if window_type == "minute":
        return now.replace(second=0, microsecond=0)
    if window_type == "day":
        return now.replace(hour=0, minute=0, second=0, microsecond=0)
    raise ValueError(f"Unsupported window type: {window_type}")


def _get_or_create_window(db: Session, user_id: int, window_type: str, window_start: datetime) -> AiUsageWindow:
    window = (
        db.query(AiUsageWindow)
        .filter(
            AiUsageWindow.user_id == user_id,
            AiUsageWindow.window_type == window_type,
            AiUsageWindow.window_start == window_start,
        )
        .with_for_update()
        .one_or_none()
    )

    if window is None:
        window = AiUsageWindow(
            user_id=user_id,
            window_type=window_type,
            window_start=window_start,
            request_count=0,
            last_request_at=window_start,
        )
        db.add(window)

    return window


def _lock_user_row(db: Session, user_id: int) -> None:
    user = (
        db.query(User)
        .filter(User.id == user_id)
        .with_for_update()
        .one_or_none()
    )

    if user is None:
        raise QuotaExceededError("Authenticated user was not found")


def reserve_ai_request(db: Session, user_id: int, now: datetime | None = None) -> QuotaStatus:
    current_time = now or datetime.now(timezone.utc)
    _lock_user_row(db, user_id)
    minute_start = _window_start(current_time, "minute")
    day_start = _window_start(current_time, "day")

    minute_window = _get_or_create_window(db, user_id, "minute", minute_start)
    day_window = _get_or_create_window(db, user_id, "day", day_start)

    if minute_window.request_count >= AI_REQUESTS_PER_MINUTE:
        raise QuotaExceededError(f"Too many AI requests. Limit is {AI_REQUESTS_PER_MINUTE} per minute.")

    if day_window.request_count >= AI_REQUESTS_PER_DAY:
        raise QuotaExceededError(f"Too many AI requests. Limit is {AI_REQUESTS_PER_DAY} per day.")

    minute_window.request_count += 1
    minute_window.last_request_at = current_time
    day_window.request_count += 1
    day_window.last_request_at = current_time

    return QuotaStatus(
        minute_used=minute_window.request_count,
        minute_limit=AI_REQUESTS_PER_MINUTE,
        day_used=day_window.request_count,
        day_limit=AI_REQUESTS_PER_DAY,
    )
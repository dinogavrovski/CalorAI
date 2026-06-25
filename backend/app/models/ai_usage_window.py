from sqlalchemy import Column, DateTime, ForeignKey, Integer, String, UniqueConstraint
from sqlalchemy.orm import relationship

from app.db.base import Base
from app.core.time import utc_now


class AiUsageWindow(Base):
    __tablename__ = "ai_usage_windows"
    __table_args__ = (
        UniqueConstraint("user_id", "window_type", "window_start", name="uq_ai_usage_window"),
    )

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    window_type = Column(String(16), nullable=False, index=True)
    window_start = Column(DateTime, nullable=False, index=True)
    request_count = Column(Integer, nullable=False, default=0)
    last_request_at = Column(DateTime, default=utc_now, nullable=False)

    user = relationship("User")
from sqlalchemy import Column, Date, ForeignKey, Integer

from app.db.base import Base


class AiUsage(Base):
    """
    Per-user, per-day AI request counter for quota enforcement. Written only by
    the server (service role); the client never sees or touches it.
    """
    __tablename__ = "ai_usage"

    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)
    day = Column(Date, primary_key=True)
    count = Column(Integer, nullable=False, default=0)

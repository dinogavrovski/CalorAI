from datetime import datetime

from sqlalchemy import Column, DateTime, Integer, String

from app.db.base import Base


class Waitlist(Base):
    """
    Landing-page waitlist signups. Kept completely separate from user accounts.
    The public site inserts here via the Supabase anon key (RLS: anon INSERT only).
    """
    __tablename__ = "waitlist"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, nullable=False, index=True)
    source = Column(String, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)

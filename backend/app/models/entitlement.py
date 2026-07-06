from datetime import datetime

from sqlalchemy import Column, DateTime, ForeignKey, Integer, String
from sqlalchemy.orm import relationship

from app.db.base import Base


class Entitlement(Base):
    """
    Subscription/premium state — deliberately its OWN table, never a column on
    users. Only the server (webhook / verified purchase) writes this; the client
    can never flip its own premium flag.
    """
    __tablename__ = "entitlements"

    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)
    tier = Column(String, nullable=False, default="free")        # 'free' | 'premium'
    status = Column(String, nullable=False, default="inactive")  # 'active' | 'expired' | 'grace' | 'inactive'
    provider = Column(String, nullable=True)                     # 'play' | 'revenuecat'
    current_period_end = Column(DateTime, nullable=True)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    user = relationship("User", back_populates="entitlement")

from sqlalchemy import Column, Float, Integer, String
from sqlalchemy.orm import relationship
from app.db.base import Base


class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    display_name = Column(String, nullable=True)

    # Biometrics
    height_cm = Column(Float, nullable=True)
    age = Column(Integer, nullable=True)
    sex = Column(String, nullable=True)          # "male" | "female" | "other"
    current_weight_kg = Column(Float, nullable=True)
    goal_weight_kg = Column(Float, nullable=True)
    weekly_goal_kg = Column(Float, nullable=True)  # negative = loss, positive = gain
    activity_level = Column(String, nullable=True) # "sedentary" | "light" | "moderate" | "very_active" | "extra_active"
    calorie_goal = Column(Integer, nullable=True, default=2000)

    meal_logs = relationship("MealLog", back_populates="user", cascade="all, delete-orphan")
    refresh_sessions = relationship("RefreshSession", back_populates="user", cascade="all, delete-orphan")
    weight_logs = relationship("WeightLog", back_populates="user", cascade="all, delete-orphan")
    saved_meals = relationship("SavedMeal", back_populates="user", cascade="all, delete-orphan")
    entitlement = relationship("Entitlement", back_populates="user", uselist=False, cascade="all, delete-orphan")

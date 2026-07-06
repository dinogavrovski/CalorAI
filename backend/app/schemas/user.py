from pydantic import BaseModel, EmailStr, Field
from typing import Optional


class UserCreate(BaseModel):
    email: EmailStr
    password: str = Field(min_length=6)


class UserLogin(BaseModel):
    email: EmailStr
    password: str


class UserResponse(BaseModel):
    id: int
    email: EmailStr
    display_name: Optional[str] = None
    calorie_goal: int = 2000
    height_cm: Optional[float] = None
    age: Optional[int] = None
    sex: Optional[str] = None
    current_weight_kg: Optional[float] = None
    goal_weight_kg: Optional[float] = None
    weekly_goal_kg: Optional[float] = None
    activity_level: Optional[str] = None

    class Config:
        from_attributes = True


class UpdateProfileRequest(BaseModel):
    display_name: Optional[str] = None
    height_cm: Optional[float] = None
    age: Optional[int] = None
    sex: Optional[str] = None
    current_weight_kg: Optional[float] = None
    goal_weight_kg: Optional[float] = None
    weekly_goal_kg: Optional[float] = None
    activity_level: Optional[str] = None
    calorie_goal: Optional[int] = None  # manual override if biometrics not set

from fastapi import APIRouter, Depends, HTTPException
from fastapi.security import OAuth2PasswordRequestForm
from pydantic import BaseModel, EmailStr
from sqlalchemy.orm import Session
from google.auth.transport.requests import Request as GoogleRequest
from google.oauth2 import id_token as google_id_token
from app.db.database import SessionLocal
from app.models.user import User
from app.core.security import hash_password, verify_password, create_access_token

router = APIRouter(prefix="/auth", tags=["Auth"])


class RegisterRequest(BaseModel):
    username: str
    email: EmailStr
    password: str


class LoginRequest(BaseModel):
    username: str
    password: str


class GoogleLoginRequest(BaseModel):
    id_token: str


def _resolve_login_email(identifier: str) -> str:
    value = identifier.strip().lower()
    if "@" not in value:
        raise HTTPException(status_code=400, detail="Use your email address to sign in")
    return value


def _authenticate_user(identifier: str, password: str, db: Session) -> User:
    login_email = _resolve_login_email(identifier)
    user = db.query(User).filter(User.email == login_email).first()

    if not user:
        raise HTTPException(status_code=401, detail="Invalid credentials")

    if not verify_password(password, user.hashed_password):
        raise HTTPException(status_code=401, detail="Invalid credentials")

    return user


def _build_auth_response(user: User) -> dict:
    token = create_access_token({"sub": str(user.id)})
    return {
        "access_token": token,
        "token_type": "bearer",
        "user": {
            "id": str(user.id),
            "username": user.email.split("@")[0],
            "email": user.email,
        },
    }


def _get_google_audiences() -> list[str]:
    import os

    candidates = [
        os.getenv("GOOGLE_WEB_CLIENT_ID"),
        os.getenv("GOOGLE_ANDROID_CLIENT_ID"),
    ]
    return [value.strip() for value in candidates if value and value.strip()]

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@router.post("/register")
def register(payload: RegisterRequest, db: Session = Depends(get_db)):
    normalized_email = payload.email.strip().lower()
    existing_user = db.query(User).filter(User.email == normalized_email).first()
    if existing_user:
        raise HTTPException(status_code=400, detail="Email already registered")

    user = User(
        email=normalized_email,
        hashed_password=hash_password(payload.password)
    )
    db.add(user)
    db.commit()
    db.refresh(user)

    token = create_access_token({"sub": str(user.id)})
    return {
        "access_token": token,
        "token_type": "bearer",
        "user": {
            "id": str(user.id),
            "username": payload.username,
            "email": user.email,
        },
    }

@router.post("/login")
def login(
    payload: LoginRequest,
    db: Session = Depends(get_db)
):
    user = _authenticate_user(payload.username, payload.password, db)
    return _build_auth_response(user)


@router.post("/google")
def google_login(payload: GoogleLoginRequest, db: Session = Depends(get_db)):
    audiences = _get_google_audiences()
    if not audiences:
        raise HTTPException(status_code=500, detail="Google auth is not configured")

    token_info = None
    last_error = None
    for audience in audiences:
        try:
            token_info = google_id_token.verify_oauth2_token(
                payload.id_token,
                GoogleRequest(),
                audience,
            )
            break
        except Exception as exc:
            last_error = exc

    if token_info is None:
        raise HTTPException(status_code=401, detail="Invalid Google token") from last_error

    email = str(token_info.get("email", "")).strip().lower()
    if not email:
        raise HTTPException(status_code=400, detail="Google account has no email")
    if not token_info.get("email_verified", False):
        raise HTTPException(status_code=400, detail="Google email is not verified")

    user = db.query(User).filter(User.email == email).first()
    if not user:
        stable_secret = str(token_info.get("sub", email))
        user = User(
            email=email,
            hashed_password=hash_password(f"google::{stable_secret}"),
        )
        db.add(user)
        db.commit()
        db.refresh(user)

    return _build_auth_response(user)


@router.post("/token")
def token_login(
    form_data: OAuth2PasswordRequestForm = Depends(),
    db: Session = Depends(get_db),
):
    user = _authenticate_user(form_data.username, form_data.password, db)
    token = create_access_token({"sub": str(user.id)})
    return {
        "access_token": token,
        "token_type": "bearer",
    }

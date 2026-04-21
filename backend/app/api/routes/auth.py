from datetime import datetime
from uuid import uuid4

from fastapi import APIRouter, Depends, HTTPException
from fastapi.security import OAuth2PasswordRequestForm
from pydantic import BaseModel, EmailStr
from sqlalchemy.orm import Session
from google.auth.transport.requests import Request as GoogleRequest
from google.oauth2 import id_token as google_id_token
from app.db.database import SessionLocal
from app.models.refresh_session import RefreshSession
from app.models.user import User
from app.core.security import (
    ACCESS_TOKEN_EXPIRE_MINUTES,
    create_access_token,
    generate_refresh_token,
    hash_password,
    hash_token,
    refresh_token_expiry_utc,
    verify_password,
)

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


class RefreshTokenRequest(BaseModel):
    refresh_token: str


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


def _create_refresh_session(user: User, db: Session) -> tuple[str, RefreshSession]:
    raw_token = generate_refresh_token()
    session = RefreshSession(
        user_id=user.id,
        token_hash=hash_token(raw_token),
        jti=uuid4().hex,
        expires_at=refresh_token_expiry_utc(),
    )
    db.add(session)
    return raw_token, session


def _build_auth_response(user: User, db: Session, username_display: str | None = None) -> dict:
    access_token = create_access_token({"sub": str(user.id)})
    refresh_token, _ = _create_refresh_session(user, db)
    db.commit()

    return {
        "access_token": access_token,
        "access_token_expires_in": ACCESS_TOKEN_EXPIRE_MINUTES * 60,
        "refresh_token": refresh_token,
        "token_type": "bearer",
        "user": {
            "id": str(user.id),
            "username": username_display or user.email.split("@")[0],
            "email": user.email,
        },
    }


def _resolve_refresh_session(raw_refresh_token: str, db: Session) -> RefreshSession:
    session = (
        db.query(RefreshSession)
        .filter(RefreshSession.token_hash == hash_token(raw_refresh_token))
        .first()
    )

    if not session:
        raise HTTPException(status_code=401, detail="Invalid refresh token")

    if session.revoked_at is not None:
        raise HTTPException(status_code=401, detail="Refresh token has been revoked")

    if session.expires_at <= datetime.utcnow():
        raise HTTPException(status_code=401, detail="Refresh token has expired")

    return session


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

    return _build_auth_response(user, db, username_display=payload.username)

@router.post("/login")
def login(
    payload: LoginRequest,
    db: Session = Depends(get_db)
):
    user = _authenticate_user(payload.username, payload.password, db)
    return _build_auth_response(user, db)


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

    return _build_auth_response(user, db)


@router.post("/refresh")
def refresh_access_token(payload: RefreshTokenRequest, db: Session = Depends(get_db)):
    old_session = _resolve_refresh_session(payload.refresh_token, db)
    user = db.query(User).filter(User.id == old_session.user_id).first()
    if not user:
        raise HTTPException(status_code=401, detail="Invalid refresh token")

    new_refresh_token, new_session = _create_refresh_session(user, db)
    old_session.revoked_at = datetime.utcnow()
    old_session.replaced_by_jti = new_session.jti
    db.commit()

    access_token = create_access_token({"sub": str(user.id)})
    return {
        "access_token": access_token,
        "access_token_expires_in": ACCESS_TOKEN_EXPIRE_MINUTES * 60,
        "refresh_token": new_refresh_token,
        "token_type": "bearer",
    }


@router.post("/logout")
def logout(payload: RefreshTokenRequest, db: Session = Depends(get_db)):
    session = (
        db.query(RefreshSession)
        .filter(RefreshSession.token_hash == hash_token(payload.refresh_token))
        .first()
    )

    if session and session.revoked_at is None:
        session.revoked_at = datetime.utcnow()
        db.commit()

    return {"ok": True}


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

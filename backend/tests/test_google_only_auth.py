import os
import tempfile
from pathlib import Path
import unittest
from unittest.mock import patch

TEST_DB_DIR = Path(tempfile.mkdtemp(prefix="calorai-auth-tests-"))
TEST_DB_PATH = TEST_DB_DIR / "test.db"

os.environ.setdefault("DATABASE_URL", f"sqlite:///{TEST_DB_PATH.as_posix()}")
os.environ.setdefault("SECRET_KEY", "test-secret")
os.environ.setdefault("ALGORITHM", "HS256")
os.environ.setdefault("GOOGLE_WEB_CLIENT_ID", "test-web-client")
os.environ.setdefault("GROQ_API_KEY", "test-groq-key")

from fastapi.testclient import TestClient  # noqa: E402

from app.db.base import Base  # noqa: E402
from app.db.database import SessionLocal, engine  # noqa: E402
from app.main import app  # noqa: E402
from app.models.refresh_session import RefreshSession  # noqa: E402
from app.models.user import User  # noqa: E402


class GoogleOnlyAuthTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        Base.metadata.create_all(bind=engine)

    def setUp(self):
        self.client = TestClient(app)

    def tearDown(self):
        with SessionLocal() as db:
            db.query(RefreshSession).delete()
            db.query(User).delete()
            db.commit()

    def test_legacy_auth_endpoints_are_removed(self):
        self.assertEqual(self.client.post("/auth/register", json={}).status_code, 404)
        self.assertEqual(self.client.post("/auth/login", json={}).status_code, 404)
        self.assertEqual(self.client.post("/auth/token", data={}).status_code, 404)

    @patch("app.api.routes.auth.google_id_token.verify_oauth2_token")
    def test_google_login_creates_user_and_returns_tokens(self, verify_token):
        verify_token.return_value = {
            "email": "googleuser@example.com",
            "email_verified": True,
            "sub": "google-sub-123",
        }

        response = self.client.post("/auth/google", json={"id_token": "id-token"})

        self.assertEqual(response.status_code, 200)
        payload = response.json()
        self.assertIn("access_token", payload)
        self.assertIn("refresh_token", payload)
        self.assertEqual(payload["user"]["email"], "googleuser@example.com")

        with SessionLocal() as db:
            user = db.query(User).filter(User.email == "googleuser@example.com").first()
            self.assertIsNotNone(user)

    @patch("app.api.routes.auth.google_id_token.verify_oauth2_token")
    def test_invalid_google_token_is_rejected(self, verify_token):
        verify_token.side_effect = Exception("invalid token")

        response = self.client.post("/auth/google", json={"id_token": "bad-token"})

        self.assertEqual(response.status_code, 401)
        self.assertEqual(response.json()["detail"], "Invalid Google token")

    @patch("app.api.routes.auth.google_id_token.verify_oauth2_token")
    def test_refresh_and_logout_still_work(self, verify_token):
        verify_token.return_value = {
            "email": "sessionuser@example.com",
            "email_verified": True,
            "sub": "google-sub-987",
        }

        login_response = self.client.post("/auth/google", json={"id_token": "id-token"})
        self.assertEqual(login_response.status_code, 200)
        refresh_token = login_response.json()["refresh_token"]

        refresh_response = self.client.post("/auth/refresh", json={"refresh_token": refresh_token})
        self.assertEqual(refresh_response.status_code, 200)
        self.assertIn("access_token", refresh_response.json())

        logout_response = self.client.post("/auth/logout", json={"refresh_token": refresh_response.json()["refresh_token"]})
        self.assertEqual(logout_response.status_code, 200)
        self.assertEqual(logout_response.json().get("ok"), True)


if __name__ == "__main__":
    unittest.main()

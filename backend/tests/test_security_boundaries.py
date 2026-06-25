import os
import tempfile
from datetime import datetime, timezone
from pathlib import Path
from types import SimpleNamespace
import unittest
from unittest.mock import patch

TEST_DB_DIR = Path(tempfile.mkdtemp(prefix="calorai-tests-"))
TEST_DB_PATH = TEST_DB_DIR / "test.db"

os.environ.setdefault("DATABASE_URL", f"sqlite:///{TEST_DB_PATH.as_posix()}")
os.environ.setdefault("SECRET_KEY", "test-secret")
os.environ.setdefault("ALGORITHM", "HS256")
os.environ.setdefault("GOOGLE_WEB_CLIENT_ID", "test-web-client")
os.environ.setdefault("GROQ_API_KEY", "test-groq-key")

from fastapi.testclient import TestClient  # noqa: E402

from app.api.routes.ai import get_current_user as ai_get_current_user  # noqa: E402
from app.db.base import Base  # noqa: E402
from app.db.database import SessionLocal, engine, get_db  # noqa: E402
from app.dependencies.auth import get_current_user  # noqa: E402
from app.main import app  # noqa: E402
from app.models.ai_usage_window import AiUsageWindow  # noqa: E402
from app.models.meal_log import MealLog  # noqa: E402
from app.models.user import User  # noqa: E402


def make_user(user_id: int, email: str) -> SimpleNamespace:
    return SimpleNamespace(id=user_id, email=email)


def override_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def utc_now() -> datetime:
    return datetime.now(timezone.utc)


class SecurityBoundaryTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        Base.metadata.create_all(bind=engine)

    def setUp(self):
        self.client = TestClient(app)
        app.dependency_overrides[get_db] = override_db
        app.dependency_overrides[get_current_user] = lambda: make_user(1, "one@example.com")
        app.dependency_overrides[ai_get_current_user] = lambda: make_user(1, "one@example.com")
        self._seed_data()

    def tearDown(self):
        app.dependency_overrides.clear()
        with SessionLocal() as db:
            db.query(AiUsageWindow).delete()
            db.query(MealLog).delete()
            db.query(User).delete()
            db.commit()

    def _seed_data(self):
        with SessionLocal() as db:
            db.add_all(
                [
                    User(id=1, email="one@example.com", hashed_password="hash-1"),
                    User(id=2, email="two@example.com", hashed_password="hash-2"),
                    MealLog(
                        id=10,
                        user_id=1,
                        note="user one meal",
                        items_json=[],
                        total_calories=250,
                        total_calorie_low=200,
                        total_calorie_high=300,
                        created_at=utc_now(),
                    ),
                    MealLog(
                        id=20,
                        user_id=2,
                        note="user two meal",
                        items_json=[],
                        total_calories=500,
                        total_calorie_low=450,
                        total_calorie_high=550,
                        created_at=utc_now(),
                    ),
                ]
            )
            db.commit()

    def test_meal_history_is_scoped_to_current_user(self):
        response = self.client.get("/user/meal-history")

        self.assertEqual(response.status_code, 200)
        payload = response.json()
        self.assertEqual(len(payload), 1)
        self.assertEqual(payload[0]["user_id"], 1)
        self.assertEqual(payload[0]["note"], "user one meal")

    def test_cannot_update_another_users_meal(self):
        response = self.client.put("/user/meal-history/20", json={"note": "steal meal"})

        self.assertEqual(response.status_code, 404)
        self.assertEqual(response.json()["detail"], "Meal not found")

    def test_ai_requests_are_throttled_per_user(self):
        with patch("app.api.routes.ai.estimate_from_text_note") as estimator:
            estimator.return_value = {
                "note": "test",
                "items": [],
                "total_calories": 1,
                "total_calorie_range": [1, 1],
            }

            first = self.client.post("/ai/log-text", json={"note": "test meal"})
            second = self.client.post("/ai/log-text", json={"note": "test meal"})
            third = self.client.post("/ai/log-text", json={"note": "test meal"})
            fourth = self.client.post("/ai/log-text", json={"note": "test meal"})

        self.assertEqual(first.status_code, 200)
        self.assertEqual(second.status_code, 200)
        self.assertEqual(third.status_code, 200)
        self.assertEqual(fourth.status_code, 429)
        self.assertIn("minute", fourth.json()["detail"])

    def test_ai_quota_is_independent_per_user(self):
        with patch("app.api.routes.ai.estimate_from_text_note") as estimator:
            estimator.return_value = {
                "note": "test",
                "items": [],
                "total_calories": 1,
                "total_calorie_range": [1, 1],
            }

            for _ in range(3):
                response = self.client.post("/ai/log-text", json={"note": "user one meal"})
                self.assertEqual(response.status_code, 200)

            app.dependency_overrides[get_current_user] = lambda: make_user(2, "two@example.com")
            app.dependency_overrides[ai_get_current_user] = lambda: make_user(2, "two@example.com")

            response = self.client.post("/ai/log-text", json={"note": "user two meal"})

        self.assertEqual(response.status_code, 200)


if __name__ == "__main__":
    unittest.main()
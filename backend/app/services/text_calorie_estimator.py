import os
from typing import Dict

from app.services.groq_text_estimator import estimate_with_groq
from app.services.gemini_estimator import estimate_with_gemini


def estimate_from_text_note(note: str) -> Dict:
    provider = os.getenv("AI_PROVIDER", "groq").strip().lower()

    if provider == "gemini":
        result = estimate_with_gemini(note)
    else:
        result = estimate_with_groq(note)

    if result is not None:
        return result

    raise RuntimeError(f"Estimation failed for provider '{provider}'")

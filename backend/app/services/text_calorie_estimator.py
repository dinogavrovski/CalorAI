from typing import Dict

from app.services.groq_text_estimator import estimate_with_groq


def estimate_from_text_note(note: str) -> Dict:
    ai_estimate = estimate_with_groq(note)
    if ai_estimate is not None:
        return ai_estimate

    raise RuntimeError("Groq estimation failed")

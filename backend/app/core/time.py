from datetime import UTC, datetime


def utc_now() -> datetime:
    # Keep naive UTC timestamps for compatibility with existing DateTime columns.
    return datetime.now(UTC).replace(tzinfo=None)
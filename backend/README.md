# Backend Setup (Supabase Ready)

## 1) Configure environment
Copy the template and edit values:

```powershell
Copy-Item backend/.env.example .env
```

Set `DATABASE_URL` to your Supabase Postgres URL in `.env`.

For Google sign-in, also set:

- `GOOGLE_WEB_CLIENT_ID`
- `GOOGLE_ANDROID_CLIENT_ID`

For AI-first text calorie estimation with Groq, set:

- `GROQ_API_KEY`
- Optional: `GROQ_MODEL` (default: `llama-3.1-8b-instant`)

Groq is the only active estimation engine right now. If it is unavailable or quota-limited, the API returns a 502 error.

## 2) Start API
From the repository root:

```powershell
uvicorn backend.app.main:app --reload
```

## 3) One-command DB connectivity check
Run this after API starts:

```powershell
curl http://127.0.0.1:8000/health/db
```

Expected successful response:

```json
{"ok":true,"database":"reachable"}
```

If it fails, verify:
- `DATABASE_URL` is present in `.env`
- Supabase password is correct
- IP/network access is allowed

# Backend Setup (Supabase Ready)

## 1) Configure environment
Copy the template and edit values:

```powershell
Copy-Item backend/.env.example .env
```

Set `DATABASE_URL` to your Supabase Postgres URL in `.env`.

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

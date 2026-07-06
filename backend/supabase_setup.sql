-- ═══════════════════════════════════════════════════════════════════════════
--  Vora · Supabase lockdown  (Phase 1: DB migration, custom JWT kept)
--
--  WHY THIS MATTERS:
--  Supabase exposes every table in the `public` schema through its auto-generated
--  Data API (PostgREST), reachable with the ANON key — and that key ships publicly
--  in the landing-page JS. Enabling Row Level Security with NO permissive policy
--  makes a table invisible to the anon/authenticated roles. Your FastAPI backend
--  connects as the Postgres owner, which BYPASSES RLS, so it keeps full access.
--
--  RUN THIS ONCE, in the Supabase SQL Editor, AFTER starting the backend a first
--  time (the backend's create_all() builds the tables; this then locks them).
-- ═══════════════════════════════════════════════════════════════════════════

-- 1) Deny all client (anon/authenticated) access to every private app table.
--    No policy is added, so RLS blocks everything for those roles.
alter table public.users            enable row level security;
alter table public.meal_logs        enable row level security;
alter table public.saved_meals      enable row level security;
alter table public.weight_logs      enable row level security;
alter table public.refresh_sessions enable row level security;
alter table public.entitlements     enable row level security;
alter table public.ai_usage         enable row level security;

-- 2) Waitlist — the ONLY table the public site touches, via the anon key.
alter table public.waitlist enable row level security;

-- Anonymous visitors may INSERT their email and nothing else.
-- (No SELECT/UPDATE/DELETE policy ⇒ they cannot read or modify the list.)
drop policy if exists "waitlist anon insert" on public.waitlist;
create policy "waitlist anon insert"
  on public.waitlist
  for insert
  to anon
  with check (true);

-- ── Sanity check (optional): list which tables have RLS on ──────────────────
-- select relname, relrowsecurity
-- from pg_class
-- where relnamespace = 'public'::regnamespace and relkind = 'r'
-- order by relname;

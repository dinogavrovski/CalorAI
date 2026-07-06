/* ─────────────────────────────────────────────────────────────────────
 * Vora landing — waitlist handling
 *
 * WIRING THE WAITLIST (Supabase):
 *   1. In Supabase → Project Settings → API, copy the Project URL and the
 *      `anon` public key.
 *   2. Paste them below. Submissions then INSERT into the `waitlist` table via
 *      PostgREST. RLS (see backend/supabase_setup.sql) allows anon INSERT only —
 *      nobody can read the list with this key.
 *   While both are empty, submissions fall back to localStorage so the form is
 *   testable during development.
 *
 *   (Later, add Cloudflare Turnstile before going fully public.)
 * ──────────────────────────────────────────────────────────────────── */
const SUPABASE_URL = "";        // e.g. https://abcdefgh.supabase.co
const SUPABASE_ANON_KEY = "";   // the public anon key (safe to expose)

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

document.getElementById("year").textContent = new Date().getFullYear();

// ── Toast ──────────────────────────────────────────────────────────────
let toastTimer;
function toast(message, kind = "success") {
  const el = document.getElementById("toast");
  el.textContent = message;
  el.className = `toast show toast--${kind}`;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { el.className = "toast"; }, 3600);
}

// ── Submit handler ─────────────────────────────────────────────────────
async function handleSubmit(form, noteEl) {
  const input = form.querySelector(".waitlist__input");
  const button = form.querySelector("button");
  const email = input.value.trim().toLowerCase();

  if (!EMAIL_RE.test(email)) {
    input.classList.add("invalid");
    input.focus();
    toast("Please enter a valid email.", "error");
    return;
  }
  input.classList.remove("invalid");

  const originalLabel = button.textContent;
  button.disabled = true;
  button.textContent = "Joining…";

  try {
    if (SUPABASE_URL && SUPABASE_ANON_KEY) {
      const res = await fetch(`${SUPABASE_URL}/rest/v1/waitlist`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          apikey: SUPABASE_ANON_KEY,
          Authorization: `Bearer ${SUPABASE_ANON_KEY}`,
          Prefer: "return=minimal",
        },
        body: JSON.stringify({ email, source: "landing" }),
      });
      // 409 = duplicate email (unique constraint) → already signed up
      if (res.status === 409) {
        input.value = "";
        toast("You're already on the list 🎉", "success");
        return;
      }
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
    } else {
      // Dev fallback: keep a local list so the form is testable.
      const key = "vora_waitlist";
      const list = JSON.parse(localStorage.getItem(key) || "[]");
      if (list.includes(email)) {
        toast("You're already on the list 🎉", "success");
        return;
      }
      list.push(email);
      localStorage.setItem(key, JSON.stringify(list));
      await new Promise((r) => setTimeout(r, 500)); // simulate latency
    }

    input.value = "";
    if (noteEl) noteEl.textContent = "You're on the list — we'll be in touch. ✨";
    toast("You're on the waitlist! 🎉", "success");
  } catch (err) {
    console.error("Waitlist submit failed:", err);
    toast("Something went wrong. Please try again.", "error");
  } finally {
    resetForm(form, input, button, originalLabel, noteEl, false);
  }
}

function resetForm(form, input, button, originalLabel, noteEl, keepNote) {
  button.disabled = false;
  button.textContent = originalLabel;
}

// ── Wire both forms ────────────────────────────────────────────────────
[
  ["waitlist-hero", "waitlist-hero-note"],
  ["waitlist-cta", "waitlist-cta-note"],
].forEach(([formId, noteId]) => {
  const form = document.getElementById(formId);
  if (!form) return;
  const noteEl = document.getElementById(noteId);
  form.addEventListener("submit", (e) => {
    e.preventDefault();
    handleSubmit(form, noteEl);
  });
  form
    .querySelector(".waitlist__input")
    .addEventListener("input", (e) => e.target.classList.remove("invalid"));
});

// ── Subtle reveal on scroll ────────────────────────────────────────────
const observer = new IntersectionObserver(
  (entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.style.opacity = "1";
        entry.target.style.transform = "none";
        observer.unobserve(entry.target);
      }
    });
  },
  { threshold: 0.12 }
);

document.querySelectorAll(".feature, .step, .cta__card").forEach((el) => {
  el.style.opacity = "0";
  el.style.transform = "translateY(16px)";
  el.style.transition = "opacity .5s ease, transform .5s ease";
  observer.observe(el);
});

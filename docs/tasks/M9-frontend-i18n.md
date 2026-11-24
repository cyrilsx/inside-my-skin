# M9 — Frontend i18n: English, French, Arabic, Chinese, Russian

Status: done

## Goal

Make the Vue dashboard fully localized in five locales — en, fr, ar, zh, ru — following the
i18n area of specification.website: a language switcher that lists each locale in its own
language (no flags), `lang` on `<html>` synced to the active locale, `dir="rtl"` for Arabic with
CSS logical properties, and locale-aware `Intl` formatting for all dates/numbers/units.

## Acceptance criteria

- [x] `vue-i18n` 11.4.23 added; `src/i18n/` with `index.ts` (createI18n, fallback `en`,
      initial locale from `localStorage('healthhub-locale')` → `navigator.language` → `en`)
      and five locale modules `locales/{en,fr,ar,zh,ru}.ts` with identical key trees.
- [x] A watcher syncs `document.documentElement.lang` and `dir` (`rtl` for `ar`) and persists
      the choice; `LanguageSwitcher.vue` in the header lists English / Français / العربية /
      中文 / Русский, each `<option>` carrying its own `lang`.
- [x] All user-facing strings go through `t()` (nav, views, dialog, 404, errors, footer);
      provider/brand names and enum values (Withings, Garmin, STEPS…) stay untranslated with
      `translate="no"`.
- [x] `src/utils/format.ts` formats dates/times/numbers/units via `Intl` with the active
      locale (not the raw ISO strings / hardcoded "min"/"km").
- [x] `style.css` uses logical properties (`inset-inline-start`, `text-align: start`) so the
      layout mirrors correctly under RTL.
- [x] Tests: `i18n.spec.ts` (locale set = {en,fr,ar,zh,ru}, key parity across locales, RTL dir
      + `lang` sync), `App.spec.ts` extended (switcher renders all five locales); 8 files /
      19 tests green; `npm run build` (vue-tsc + vite) green; secret scan green; backend and
      android untouched.

## Dependencies

M0–M8 merged. Frontend lane only; no API contract change.

## Lane

Frontend. Verification: `cd frontend && npm run test && npm run build`, secret scan, CI.

## Notes

- Mental-wellbeing copy stays descriptive-only in every locale (AGENTS.md rule): labels like
  "mood valence" / "screen time (estimate)" are translated as neutral descriptions.
- SEO hreflang/sitemaps stay N/A: the app is private (noindex, robots disallow from M8).

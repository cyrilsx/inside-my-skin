# M8 — Frontend audit against specification.website ("What a good website does")

Status: done

## Goal

Audit the Vue dashboard against the Website Specification (https://specification.website/, 186
topics, 10 areas, read via its open MCP tools) and implement the items that apply to an
authenticated, single-user health dashboard — while explicitly marking the ones that don't
(public SEO/OG, analytics, third-party scripts, cookies) and why.

## Acceptance criteria

- [x] `index.html`: meta description, `robots=noindex,nofollow` (private health data must never be
      indexed), `referrer=no-referrer`, theme-color (light+dark), color-scheme, canonical, favicon
      links, manifest link, `<noscript>` fallback; CSP meta injected only in production builds
      (script-src 'self', style-src 'self', connect-src 'self', object-src 'none', base-uri 'self',
      upgrade-insecure-requests).
- [x] `public/`: favicon.svg + favicon.ico, 192/512 PWA icons, apple-touch-icon,
      manifest.webmanifest, robots.txt (`Disallow: /`), `.well-known/security.txt`.
- [x] Global `src/style.css`: color-scheme-aware contrast-safe palette (light + dark), visible
      `:focus-visible` indicators, skip-link styling, `scrollbar-gutter: stable`, balanced heading
      text, 24px+ touch targets, `prefers-reduced-motion` handling.
- [x] `App.vue`: skip link, `<header>`/`<nav aria-label>`/`<main id="main">`/`<footer>` landmarks,
      `aria-current="page"` on the active nav link.
- [x] Router: lazy-loaded views (code splitting), catch-all 404 route → `NotFoundView`; global
      `app.config.errorHandler` surfaced in an aria-live region.
- [x] Views: real `<table>` gets `<caption>` + `scope="col"`; dates/numbers/durations formatted
      with `Intl` (new `src/utils/format.ts`); delete confirmation uses a native `<dialog>`
      (`ConfirmDialog.vue`) instead of `window.confirm`; ECharts options disable animation under
      `prefers-reduced-motion`.
- [x] Tests: `format.spec.ts`, router 404 spec; existing specs still green; `npm run test` and
      `npm run build` (vue-tsc + vite) green; secret scan green; backend/android untouched, CI green.

## Deliberately not applicable (private, authenticated, self-hosted SPA)

- SEO/OG/structured data/sitemaps/feeds: the app is private — we ship noindex + robots disallow
  instead of indexing signals.
- Cookie consent / GPC / analytics: no cookies (JWT kept in memory, sent as a header), no
  analytics, no third-party scripts (so SRI is trivially satisfied — everything is bundled).
- HSTS/TLS/COOP/frame-ancestors/reporting-endpoints: response headers belong to the hosting
  server/proxy, not the SPA — recorded as an infra follow-up, not faked in a meta tag
  (frame-ancestors is ignored in `<meta>`).
- Passkeys/WebAuthn, well-known OIDC/webfinger/assetlinks, agent-readiness catalog files:
  no accounts beyond the single owner, no federation, private content must not be agent-indexed.
- Service worker/offline: optional tier; noted as follow-up.
- CSP `connect-src` covers same-origin API; if `VITE_API_BASE_URL` is ever pointed cross-origin,
  that origin must be added to the CSP (documented in the PR).

## Dependencies

M0–M7 merged. Frontend lane only; no contract change (generated client untouched).

## Lane

Frontend. Verification: `cd frontend && npm run test && npm run build`, secret scan, CI.

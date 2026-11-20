#!/usr/bin/env bash
set -euo pipefail

PATTERNS='BEGIN (RSA |EC |DSA |OPENSSH |PGP )?PRIVATE KEY|AKIA[0-9A-Z]{16}|ghp_[A-Za-z0-9]{20,}|github_pat_[A-Za-z0-9_]{20,}|xox[baprs]-[A-Za-z0-9-]{10,}|AIza[0-9A-Za-z_-]{35}|sk-[A-Za-z0-9]{20,}|eyJ[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}'

if git grep -n -I -E "$PATTERNS" -- . ':!infra/scripts/scan-secrets.sh'; then
  echo "Secret-like patterns found in tracked files. Remove them and rotate the exposed credential." >&2
  exit 1
fi

echo "No secret-like patterns found in tracked files."

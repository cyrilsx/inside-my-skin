/**
 * Console logger with mandatory redaction: tokens, secrets and credential values are masked
 * before anything reaches the console (which ends up in browser logs / crash reports).
 */
const SENSITIVE_KEY = /(secret|token|password|passwd|api[-_]?key|personal[-_]?key|authorization|credentials|code|state|client[-_]?id|user[-_]?id)/i
const PAIR = /("[^"]*"|'[^']*'|[A-Za-z0-9_.-]+)(\s*[:=]\s*)("[^"]*"|'[^']*'|[^\s,;&]+)/g
const BEARER = /((?:bearer|token)\s+)[^\s,;&]+/gi

export function redact(input: string): string {
  if (!input) return input
  return input
    .replace(BEARER, '$1***')
    .replace(PAIR, (m, key: string, sep: string, value: string) => {
      if (!SENSITIVE_KEY.test(key.replace(/["']/g, ''))) return m
      const masked = value.startsWith('"') || value.startsWith("'") ? `${value[0]}***${value[0]}` : '***'
      return `${key}${sep}${masked}`
    })
}

export const logger = {
  info: (msg: string, ...args: unknown[]) => console.info(redact(msg), ...args.map(a => typeof a === 'string' ? redact(a) : a)),
  warn: (msg: string, ...args: unknown[]) => console.warn(redact(msg), ...args.map(a => typeof a === 'string' ? redact(a) : a)),
  error: (msg: string, ...args: unknown[]) => console.error(redact(msg), ...args.map(a => typeof a === 'string' ? redact(a) : a))
}

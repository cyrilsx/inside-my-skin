import { describe, expect, it } from 'vitest'
import { redact } from './logger'

describe('redact', () => {
  it('masks credentials in JSON and bearer tokens', () => {
    const out = redact('{"clientSecret":"abc123","provider":"garmin"}')
    expect(out).not.toContain('abc123')
    expect(out).toContain('"clientSecret":"***"')
    expect(out).toContain('"provider":"garmin"')
    expect(redact('Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.x.y')).not.toContain('eyJhbGciOiJIUzI1NiJ9')
  })

  it('masks query params and leaves harmless text alone', () => {
    const out = redact('/callback?code=xyz&foo=bar')
    expect(out).not.toContain('xyz')
    expect(out).toContain('foo=bar')
    expect(redact('loaded 42 items')).toBe('loaded 42 items')
  })
})

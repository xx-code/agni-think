import { describe, it, expect, vi, afterEach } from 'vitest'
import { formatDate, getDaysRemaining } from '~/utils/formatDate'

describe('formatDate', () => {
  it('formats a date with French locale', () => {
    const date = new Date('2024-03-15T12:00:00Z')
    const result = formatDate(date)
    expect(result).toContain('mar')
    expect(result).toContain('2024')
  })

  it('formats different months', () => {
    const jan = formatDate(new Date('2024-01-01T00:00:00Z'))
    expect(jan).toContain('janv')

    const dec = formatDate(new Date('2024-12-25T00:00:00Z'))
    expect(dec).toContain('déc')
  })
})

describe('getDaysRemaining', () => {
  afterEach(() => {
    vi.useRealTimers()
  })

  it('returns positive days for future date', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2024-06-01T00:00:00Z'))

    const future = new Date('2024-06-11T00:00:00Z')
    expect(getDaysRemaining(future)).toBe(10)
  })

  it('returns 0 for same day', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2024-06-01T00:00:00Z'))

    const sameDay = new Date('2024-06-01T00:00:00Z')
    expect(getDaysRemaining(sameDay)).toBe(0)
  })

  it('returns negative days for past date', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2024-06-11T00:00:00Z'))

    const past = new Date('2024-06-01T00:00:00Z')
    expect(getDaysRemaining(past)).toBe(-10)
  })

  it('accepts date strings', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2024-06-01T00:00:00Z'))

    expect(getDaysRemaining('2024-06-06T00:00:00Z')).toBe(5)
  })
})

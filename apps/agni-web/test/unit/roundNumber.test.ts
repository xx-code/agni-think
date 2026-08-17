import { describe, it, expect } from 'vitest'
import { roundNumber } from '~/utils/roundNumber'

describe('roundNumber', () => {
  it('rounds to 2 decimal places by default', () => {
    expect(roundNumber(3.14159)).toBe(3.14)
    expect(roundNumber(2.005)).toBe(2)
    expect(roundNumber(1.005)).toBe(1)
  })

  it('rounds to specified decimal places', () => {
    expect(roundNumber(3.14159, 3)).toBe(3.142)
    expect(roundNumber(3.14159, 1)).toBe(3.1)
    expect(roundNumber(3.14159, 0)).toBe(3)
  })

  it('returns a number, not a string', () => {
    expect(typeof roundNumber(3.14)).toBe('number')
  })

  it('handles negative numbers', () => {
    expect(roundNumber(-3.14159)).toBe(-3.14)
    expect(roundNumber(-3.14159, 1)).toBe(-3.1)
  })

  it('handles zero', () => {
    expect(roundNumber(0)).toBe(0)
  })
})

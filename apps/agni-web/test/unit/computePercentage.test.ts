import { describe, it, expect } from 'vitest'
import computePercentage from '~/utils/computePercentage'

describe('computePercentage', () => {
  it('returns 0 when remainder is 0', () => {
    expect(computePercentage(100, 0)).toBe(0)
  })

  it('returns 0 when total is 0', () => {
    expect(computePercentage(0, 50)).toBe(0)
  })

  it('returns 0 when both are 0', () => {
    expect(computePercentage(0, 0)).toBe(0)
  })

  it('returns correct percentage', () => {
    expect(computePercentage(200, 50)).toBe(25)
    expect(computePercentage(100, 100)).toBe(100)
    expect(computePercentage(100, 25)).toBe(25)
  })

  it('returns percentage over 100 when limitTo100 is false', () => {
    expect(computePercentage(50, 100, false)).toBe(200)
    expect(computePercentage(10, 15, false)).toBe(150)
  })

  it('caps at 100 when limitTo100 is true (default)', () => {
    expect(computePercentage(50, 100)).toBe(100)
    expect(computePercentage(10, 15)).toBe(100)
  })

  it('caps at 100 when limitTo100 is explicitly true', () => {
    expect(computePercentage(50, 100, true)).toBe(100)
  })

  it('does not cap when limitTo100 is explicitly false', () => {
    expect(computePercentage(50, 100, false)).toBe(200)
  })
})

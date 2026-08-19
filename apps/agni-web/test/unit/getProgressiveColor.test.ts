import { describe, it, expect } from 'vitest'
import { getProgressColor } from '~/utils/getProgressiveColor'

describe('getProgressColor', () => {
  it('returns success for low percentages', () => {
    expect(getProgressColor(0)).toBe('success')
    expect(getProgressColor(30)).toBe('success')
    expect(getProgressColor(69)).toBe('success')
  })

  it('returns warning for mid-range percentages', () => {
    expect(getProgressColor(70)).toBe('warning')
    expect(getProgressColor(80)).toBe('warning')
    expect(getProgressColor(89)).toBe('warning')
  })

  it('returns error for high percentages', () => {
    expect(getProgressColor(90)).toBe('error')
    expect(getProgressColor(95)).toBe('error')
    expect(getProgressColor(100)).toBe('error')
  })
})

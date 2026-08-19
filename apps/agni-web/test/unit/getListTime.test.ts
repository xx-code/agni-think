import { describe, it, expect, vi, afterEach } from 'vitest'
import { getListTime } from '~/utils/getListTime'

describe('getListTime', () => {
  afterEach(() => {
    vi.useRealTimers()
  })

  it('returns month labels for Month period', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2024-06-15T00:00:00Z'))

    const result = getListTime('Month', { count: 3, spacing: 1 })
    expect(result).toHaveLength(3)
    expect(result).toEqual(['Mar', 'Apr', 'May'])
  })

  it('returns year labels for Year period', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2024-06-15T00:00:00Z'))

    const result = getListTime('Year', { count: 3, spacing: 1 })
    expect(result).toEqual(['An 2022', 'An 2023', 'An 2024'])
  })

  it('returns week labels for Week period', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2024-06-15T00:00:00Z'))

    const result = getListTime('Week', { count: 2, spacing: 1 })
    expect(result).toHaveLength(2)
    expect(result[0]).toMatch(/^Semaine \d+$/)
  })

  it('handles spacing for months', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2024-06-15T00:00:00Z'))

    const result = getListTime('Month', { count: 3, spacing: 2 })
    expect(result).toHaveLength(3)
  })

  it('handles spacing for years', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2024-06-15T00:00:00Z'))

    const result = getListTime('Year', { count: 3, spacing: 2 })
    expect(result).toEqual(['An 2020', 'An 2022', 'An 2024'])
  })

  it('returns empty array for count 0', () => {
    const result = getListTime('Month', { count: 0, spacing: 1 })
    expect(result).toEqual([])
  })
})

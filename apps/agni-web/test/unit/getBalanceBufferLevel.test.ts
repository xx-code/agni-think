import { describe, it, expect } from 'vitest'
import { getBalanceBufferLevel } from '~/utils/getBalanceBufferLevel'

describe('getBalanceBufferLevel', () => {
  it('returns success when total balance reaches the buffer', () => {
    expect(getBalanceBufferLevel(1000, 1000)).toBe('success')
    expect(getBalanceBufferLevel(1000, 1500)).toBe('success')
  })

  it('returns warning when total balance is near the buffer', () => {
    expect(getBalanceBufferLevel(1000, 800)).toBe('warning')
    expect(getBalanceBufferLevel(1000, 750)).toBe('warning')
  })

  it('returns error when the distance to the buffer is big', () => {
    expect(getBalanceBufferLevel(1000, 749)).toBe('error')
    expect(getBalanceBufferLevel(1000, 0)).toBe('error')
    expect(getBalanceBufferLevel(1000, -5000)).toBe('error')
  })

  it('returns error when no buffer is configured and total balance is negative', () => {
    expect(getBalanceBufferLevel(0, 0)).toBe('success')
    expect(getBalanceBufferLevel(0, -1)).toBe('error')
  })

  it('respects a custom near buffer ratio', () => {
    expect(getBalanceBufferLevel(1000, 500, 0.75)).toBe('warning')
    expect(getBalanceBufferLevel(1000, 500, 0.25)).toBe('error')
  })
})

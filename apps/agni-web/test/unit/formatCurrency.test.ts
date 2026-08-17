import { describe, it, expect } from 'vitest'
import formatCurrency from '~/utils/formatCurrency'

describe('formatCurrency', () => {
  it('formats zero correctly', () => {
    expect(formatCurrency(0)).toMatch(/\$0\.00/)
  })

  it('formats positive integers', () => {
    expect(formatCurrency(100)).toMatch(/100\.00/)
    expect(formatCurrency(1234567)).toMatch(/1,234,567\.00/)
  })

  it('formats positive decimals', () => {
    expect(formatCurrency(19.99)).toMatch(/19\.99/)
    expect(formatCurrency(1234.5)).toMatch(/1,234\.50/)
  })

  it('formats negative values with minus sign', () => {
    expect(formatCurrency(-50)).toMatch(/-.*50\.00/)
    expect(formatCurrency(-1234.56)).toMatch(/-.*1,234\.56/)
  })

  it('formats large numbers with commas', () => {
    expect(formatCurrency(1000000)).toMatch(/1,000,000\.00/)
  })
})

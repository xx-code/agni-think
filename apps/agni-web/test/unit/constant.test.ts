import { describe, it, expect } from 'vitest'
import { periodOptions } from '~/utils/constant'

describe('periodOptions', () => {
  it('has 6 options', () => {
    expect(periodOptions).toHaveLength(6)
  })

  it('has correct structure for each option', () => {
    periodOptions.forEach(option => {
      expect(option).toHaveProperty('label')
      expect(option).toHaveProperty('period')
      expect(option).toHaveProperty('interval')
      expect(typeof option.label).toBe('string')
      expect(['Week', 'Month', 'Year']).toContain(option.period)
      expect(typeof option.interval).toBe('number')
    })
  })

  it('has expected labels', () => {
    const labels = periodOptions.map(o => o.label)
    expect(labels).toContain('Semaine')
    expect(labels).toContain('Mois')
    expect(labels).toContain('Année')
  })

  it('has expected periods', () => {
    const periods = periodOptions.map(o => o.period)
    expect(periods).toContain('Week')
    expect(periods).toContain('Month')
    expect(periods).toContain('Year')
  })
})

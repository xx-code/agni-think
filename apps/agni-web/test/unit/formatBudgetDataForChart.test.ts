import { describe, it, expect } from 'vitest'
import { formatBudgetDataForChart } from '~/utils/formatBudgetDataForChart'
import type { BudgetType } from '~/types/ui/budget'

function makeBudget(partial: Partial<BudgetType>): BudgetType {
  return {
    id: 'budget-id',
    title: '',
    target: 0,
    currentBalance: 0,
    dueDate: new Date('2026-01-01'),
    ...partial,
  }
}

describe('formatBudgetDataForChart', () => {
  it('returns empty chart data when budgets is undefined', () => {
    const result = formatBudgetDataForChart()
    expect(result.labels).toEqual([])
    expect(result.datasets).toEqual([{
      label: 'Budgets',
      data: [],
      backgroundColor: [],
    }])
  })

  it('formats a single budget correctly', () => {
    const result = formatBudgetDataForChart([
      makeBudget({ title: 'Groceries', target: 500, currentBalance: 250 }),
    ])
    expect(result.labels).toEqual(['Groceries - 50.0%'])
    expect(result.datasets[0]!.data).toEqual([500])
    expect(result.datasets[0]!.backgroundColor).toEqual(['rgba(102,85,215, 0.5)'])
  })

  it('formats multiple budgets correctly', () => {
    const result = formatBudgetDataForChart([
      makeBudget({ title: 'Rent', target: 1000, currentBalance: 1000 }),
      makeBudget({ title: 'Fun', target: 200, currentBalance: 0 }),
    ])
    expect(result.labels).toEqual(['Rent - 100.0%', 'Fun - 0.0%'])
    expect(result.datasets[0]!.data).toEqual([1000, 200])
    expect(result.datasets[0]!.backgroundColor).toEqual([
      'rgba(102,85,215, 1)',
      'rgba(102,85,215, 0)',
    ])
  })

  it('clamps alpha between 0 and 1 when currentBalance exceeds target', () => {
    const result = formatBudgetDataForChart([
      makeBudget({ title: 'Over', target: 100, currentBalance: 150 }),
    ])
    expect(result.labels).toEqual(['Over - 150.0%'])
    expect(result.datasets[0]!.backgroundColor).toEqual(['rgba(102,85,215, 1.5)'])
  })

  it('returns datasets with correct structure', () => {
    const result = formatBudgetDataForChart([])
    expect(result.datasets).toHaveLength(1)
    expect(result.datasets[0]!.label).toBe('Budgets')
  })
})

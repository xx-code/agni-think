import type { ForcastSpendingAchieveItemResponse, GetForcastSpendingPeriodResponse, GetSpendingPeriodResponse, GetSpendingPeriodSnapshotResponse } from "../api/spending-period"

export type SpendingPeriodSnapshot = GetSpendingPeriodSnapshotResponse

export type ForcastSpendingAchieveItem = Omit<ForcastSpendingAchieveItemResponse, 'achieved'> & {
    isAchieved: boolean
}

export type ForcastSpendingPeriod = Omit< GetForcastSpendingPeriodResponse, 
    'incomeItems' | 'fixExpenseItems' | 'variableExpenseItems' | 'achievedWishedItems'> & {
    incomeItems: ForcastSpendingAchieveItem[]
    fixExpenseItems: ForcastSpendingAchieveItem[]
    variableExpenseItems: ForcastSpendingAchieveItem[]
    achievedWishedItems: ForcastSpendingAchieveItem[]
} 



export type SpendingPeriod = Omit<GetSpendingPeriodResponse, 'startDate' | 'endDate' | 'snapshot' | 'forcast'> & {
    startDate: Date
    endDate: Date
    snapshot: SpendingPeriodSnapshot
    forcast: ForcastSpendingPeriod
}

export type AllSpendingPeriod = Omit<SpendingPeriod, 'forcast'>
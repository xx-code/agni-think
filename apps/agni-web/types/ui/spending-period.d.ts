import type { SpendingPeriodType } from "../constants/spendingPeriod"
import type { ForcastSpendingAchieveItemResponse, GetForcastSpendingPeriodResponse, GetSpendingPeriodResponse, GetSpendingPeriodSnapshotResponse, GetSpendingPeriodItemResponse } from "../api/spending-period"

export type SpendingPeriodSnapshot = GetSpendingPeriodSnapshotResponse

export type ForcastSpendingAchieveItem = ForcastSpendingAchieveItemResponse

export type ForcastSpendingPeriod = GetForcastSpendingPeriodResponse

export type SpendingPeriodItem = GetSpendingPeriodItemResponse

export type SpendingPeriod = Omit<GetSpendingPeriodResponse, 'startDate' | 'endDate' | 'state'> & {
    startDate: Date
    endDate: Date
    state: SpendingPeriodType
}

export type AllSpendingPeriod = Omit<SpendingPeriod, 'forcast'>

export type EditSpendingPeriodType = {
    freeAmount: number
    savingRateTarget: number
    totalExpectedIncome: number
    totalExpectedExpenses: number
    wantSpendingItems: SpendingPeriodItem[]
}

import type { SpendingPeriodType } from "../constants/spendingPeriod"
import type { ForcastSpendingAchieveItemResponse, GetForcastSpendingPeriodResponse, GetSpendingPeriodResponse, GetSpendingPeriodSnapshotResponse, GetSpendingPeriodItemResponse, GetAllSpendingPeriodResponse } from "../api/spending-period"

export interface SpendingPeriodSnapshot extends GetSpendingPeriodSnapshotResponse {}

type ForcastSpendingAchieveItemAddon = Omit<ForcastSpendingAchieveItemResponse, 'archieved' | 'closed'> & {
    isArchieved: boolean
    isClosed: boolean
}
export interface ForcastSpendingAchieveItem extends ForcastSpendingAchieveItemAddon {}

type ForcaseSpendingAddon = Omit<GetForcastSpendingPeriodResponse, | 'incomeItems' | 'fixExpenseItems' | 
'variableExpenseItems' | 'achievedWishedItems'> & {
    incomeItems: ForcastSpendingAchieveItem[]
    fixExpenseItems: ForcastSpendingAchieveItem[]
    variableExpenseItems: ForcastSpendingAchieveItem[]
    achievedWishedItems: ForcastSpendingAchieveItem[]
}
export interface ForcastSpendingPeriod extends ForcaseSpendingAddon {}

export interface SpendingPeriodItem extends GetSpendingPeriodItemResponse {}


type GetSpendingPeriod = Omit<GetSpendingPeriodResponse, 'startDate' | 'endDate' | 'state' |'forcast' >& {
    startDate: Date
    endDate: Date
    state: SpendingPeriodType
    forcast?: ForcastSpendingPeriod
}
export interface SpendingPeriod extends GetSpendingPeriod {}

export interface AllSpendingPeriod extends Omit<SpendingPeriod, 'totalExpectedExpenses' | 'totalExpectedIncome' | 'forcast'> {}

export interface FormSpendingPeriod  extends Omit{
    savingRateTarget: number
    scheduleIncomesSelected: string[]
    scheduleExpensesSelected: string[]
    budgetsSelected: string[]
    wishlistItemSelected: string[]
}

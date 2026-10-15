export type GetSpendingPeriodSnapshotResponse = {
    income: number
    fixExpenses: number
    variableExpenses: number
    budgetExpenses: number
    saving: number
}

export type ForcastSpendingAchieveItemResponse = {
    description: string
    amount: number
    validAmount: number
    achieved: boolean
    closed: boolean
}

export type GetForcastSpendingPeriodResponse = {
    expectedRemainAmount: number
    currentRemainAmount: number
    totalExpectedIncome: number
    totalExpectedExpense: number
    expectedFixExpense: number
    expectedVariableExpense: number
    expectedBudgetExpense: number
    currentIncome: number
    expectedSaving: number
    currentSaving: number
    currentBudgetExpense: number
    incomeItems: ForcastSpendingAchieveItemResponse[]
    fixExpenseItems: ForcastSpendingAchieveItemResponse[]
    variableExpenseItems: ForcastSpendingAchieveItemResponse[]
    achievedWishedItems: ForcastSpendingAchieveItemResponse[]
}

export type GetSpendingPeriodItemResponse = {
    description: string
    amount: number
}


export type GetSpendingPeriodResponse = {
    id: string
    spendingPeriodTemplateId: string
    startDate: string
    endDate: string
    freeAmount: number
    savingRateTarget: number
    totalExpectedIncome: number
    totalExpectedExpenses: number
    state: string
    wishlistItems: string[]
    scheduleIncomesSelected: string[]
    scheduleExpensesSelected: string[]
    budgetsSelected: string[]
    snapshot: GetSpendingPeriodSnapshotResponse 
    forcast?: GetForcastSpendingPeriodResponse | null
}

export type GetAllSpendingPeriodResponse = Omit<GetSpendingPeriodResponse, 'totalExpectedExpenses' | 'totalExpectedIncome'  | 'forcast'>

export type UpdateSpendingPeriodItemRequest = {
    description: string
    amount: number
}

export type UpdateSpendingPeriodRequest = {
    spendingPeriodTemplateId?: string
    savingRateTarget?: number
    scheduleIncomesSelected?: string[]
    scheduleExpensesSelected?: string[]
    budgetsSelected?: string[]
    wishlistItemSelected?: string[]
    state?: string
}

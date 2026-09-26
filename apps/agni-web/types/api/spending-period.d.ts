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
    wantSpendingItems: {
        description: string
        amount: number
    }[]
    snapshot: GetSpendingPeriodSnapshotResponse 
    forcast: GetForcastSpendingPeriodResponse 
}

export type GetAllSpendingPeriodResponse = Omit<GetSpendingPeriodResponse, 'forcast'>
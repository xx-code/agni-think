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
    isAchieved: boolean
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

// The backend serializes the state with the enum name, the update endpoint expects the enum value
export type GetSpendingPeriodStateResponse = 'DRAFT' | 'TO_REVIEW' | 'IN_PROGRESS' | 'COMPLETE'

export type GetSpendingPeriodResponse = {
    id: string
    spendingPeriodTemplateId: string
    startDate: string
    endDate: string
    freeAmount: number
    savingRateTarget: number
    totalExpectedIncome: number
    totalExpectedExpenses: number
    state: GetSpendingPeriodStateResponse
    wantSpendingItems: GetSpendingPeriodItemResponse[]
    snapshot: GetSpendingPeriodSnapshotResponse 
    forcast?: GetForcastSpendingPeriodResponse | null
}

export type GetAllSpendingPeriodResponse = Omit<GetSpendingPeriodResponse, 'forcast'>

export type UpdateSpendingPeriodItemRequest = {
    description: string
    amount: number
}

export type UpdateSpendingPeriodRequest = {
    spendingPeriodTemplateId?: string
    freeAmount?: number
    savingRateTarget?: number
    totalExpectedIncome?: number
    totalExpectedExpenses?: number
    state?: string
    wantSpendingItems?: UpdateSpendingPeriodItemRequest[]
}

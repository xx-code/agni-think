export type GetEstimationLeftAmountResponse = {
    estimateAmount: number
    balanceScheduleIncome: number
    balanceScheduleSpend: number
    balanceFreezeSchedule: number
    balanceFreeze: number
    balanceBudget: number
}

export type GetEstimationLeftAmountRequest = {
    startDate: string
    endDate: string
}

// export type GetIncomeByDescription = {
//     label: string
//     income: number
// }


// Saving
export type GetSavingAnalysticRequest = {
    period: string
    interval: number
    startDate: string 
}
export type GetSavingAnalysticResponse = {
    savings: number[]
    investments: number[]
    savingRates: number[] 
    investmentRates: number[]
}

// CATEGORY
type GetSpendCategoryResponse = {
    categoryId: string
    spends: number[]
}

export type GetSpendCategoryRequest = {
    period: string
    interval: number
    startDate: string 
    offset: number
    limit: number
    queryAll: boolean
}

// TAG
export type GetSpendTagRequest = {
    period: string
    interval: number
    startDate: string 
    offset: number
    limit: number
    queryAll: boolean
    categoryId?: string
}

type GetSpendTagResponse = {
    tagId: string
    spends: number[]
}

// annual outlook
type GetSpendByCategoryOutlook = {
    categoryId: string
    spend: number
}
type GetAnnualOutlookResponse = {
    incomeOutlook: number,
    spendOutlook: number,
    budgetOutlook: number,
    savingMargin: number,
    currentIncomeOutlook: number,
    currentSpendOutlook: number,
    currentBudgetOutlook: number,
    currentSaving: number,
    spendByCategoriesOutlook: GetSpendByCategoryOutlook[],
    currentSpendByCategoryOutlook: GetSpendByCategoryOutlook[]
}

// budgeting rule
type GetBudgetingRuleRequest = {
    period?: string // Year, Month, Week, Day
    interval?: number = 0
    startDate?: string
    endDate?: string
}

type GetBudgetingRuleResponse = {
    ratioSaving: number
    ratioFixCost: number
    ratioVariableCost: number
    savingAmount: number
    fixCost: number
    variableCost: number
    income: number
}
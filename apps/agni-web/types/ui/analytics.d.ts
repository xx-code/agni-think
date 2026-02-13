export type EstimationLeftAmountType = {
    estimateAmount: number
}

export type GetCashflowAnalyseType = {
    spends: number[]
    incomes: number[]
}

export type GetBudgetRuleType = {
    transactionType: string
    value: number
}

export type GetAnalyseBudgeRuleType = {
    distributionSpends: GetBudgetRuleType[][]
}

export type IncomeByDescriptionType = {
    label: string
    income: number
}

export type IncomeAnalysticType = {
    incomes: number[]
    incomesByDescription: IncomeByDescriptionType[][]
}

export type SavingAnalysticType = {
    savings: number[]
    investments: number[]
    savingRates: number[] 
    investementRates: number[]
}

export type SpendTagType = {
    tagId: string
    spend: number
}

export type SpendCategoryType = {
    categoryId: string
    spendBytags: SpendTagType[]
}

export type SpendAnalysticType = {
    totalSpends: number[] 
    spendByCategories: SpendCategoryType[][] 
}
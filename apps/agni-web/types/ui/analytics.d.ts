export type EstimationLeftAmountType = {
    estimateAmount: number
}

export type GetCashflowAnalyseType = {
    spends: number[]
    incomes: number[]
}

export type GetAnalyseBudgeRuleType = {
    transactionType: string
    value: number
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
    investements: number[]
    savingRates: number[] 
    investementRates: number[]
}

export type SpendTagType = {
    tagId: string
    spend: number
}

export type SpendCategoryType = {
    categoryId: string
    spend: number
    spendBytag: SpendTagType[]
}

export type SpendAnalysticType = {
    totalSpends: number[] 
    spendByCategories: SpendCategoryType[][] 
}
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

export type GetCashflowAnalyseResponse = {
    spendFlow: number[]
    gainsFlow: number[]
}

export type GetBudgetRule = {
    type: string
    value: number
}

export type GetAnalyseBudgeRuleResponse = {
    distributionSpends: GetBudgetRule[][]
}

export type GetAnalyseBudgeRuleRequest = {
    period: string
    periodTime: number
    showNumber: number
}

// export type GetIncomeByDescription = {
//     label: string
//     income: number
// }

export type GetIncomeAnalysticRequest = {
    period: string
    periodTime: number
    showNumber: number
}
export type GetIncomeAnalysticResponse = {
    incomes: number[]
    incomesByDescription: {label: string, income: number}[][]
}

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

type GetSpendTag = {
    tagId: string
    spend: number
}

type GetSpendCategory = {
    categoryId: string
    spend: number
    spendBytags: GetSpendTag[]
}

export type GetSpendAnalysticRequest = {
    period: string
    interval: number
    startDate: string 
}
export type GetSpendAnalysticResponse = {
    totalSpend: number[] 
    spendByCategories: GetSpendCategory[][] 
}
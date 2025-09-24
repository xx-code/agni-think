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

export type GetAnalyseBudgeRuleResponse = {
    transactionType: string
    value: number
}

export type GetAnalyseBudgeRuleRequest = {
    period: string
    periodTime: number
    beginDate?: string
}

// export type GetIncomeByDescription = {
//     label: string
//     income: number
// }

export type GetIncomeAnalysticRequest = {
    period: string
    periodTime: number
    beginDate?: string
}
export type GetIncomeAnalysticResponse = {
    incomes: number[]
    incomesByDescription: {label: string, income: number}[][]
}

export type GetSavingAnalysticRequest = {
    period: string
    periodTime: number
    beginDate?: string
}
export type GetSavingAnalysticResponse = {
    savings: number[]
    investements: number[]
    savingRates: number[] 
    investementRates: number[]
}

type GetSpendTag = {
    tagId: string
    spend: number
}

type GetSpendCategory = {
    categoryId: string
    spend: number
    spendBytag: GetSpendTag[]
}

export type GetSpendAnalysticRequest = {
    period: string
    periodTime: number
    beginDate?: string 
}
export type GetSpendAnalysticResponse = {
    totalSpends: number[] 
    spendByCategories: GetSpendCategory[][] 
}
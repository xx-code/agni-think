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
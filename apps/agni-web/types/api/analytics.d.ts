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
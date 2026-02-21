export type AgentAdvisorRequest = {
    amountToAllocate: number
    futureAmountToAllocate: number
    estimationPeriodStart: string
    estimationPeriodEnd: string
    wishSpends: { amount: number, description: string } []
}


export type AgentSavegoalAdviceResponse = {
    saveGoalId: string,
    title: string,
    amountSuggest: number,
    reason: string
}

export type AgentPlanningAdvisorResponse = {
    comment: string
    suggestGoalPlanning: AgentSavegoalAdviceResponse[] 
}

export type CashflowRequest = {
    period: string
    periodTime: number
    showNumber: number
}

export type ChatPersonnalFinanceAdvisorRequest = {
    model: string
    question: string
}

export type ChatPersonnalFinanceAdvisorResponse = {
    message: string
}
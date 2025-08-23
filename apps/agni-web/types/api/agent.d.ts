export type AgentAdvisorRequest = {
    comment: string
    estimationPeriodStart: string
    estimationPeriodEnd: string
    wishSpends: { amount: number, description: string } []
    wishGoals: { goalId: string, amountSuggest: number} []
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



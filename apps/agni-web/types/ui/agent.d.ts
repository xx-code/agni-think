
export type AgentSavegoalAdviceType = {
    saveGoalId: string,
    title: string,
    amountSuggest: number,
    reason: string
}

export type PlanningAgentAdvisorType = {
    comment: string
    suggestGoalPlanning: AgentSavegoalAdviceType[] 
}


import { IAgent } from "./interface"

export type AgentPlanningAdvisorOutput = {
    goals:{
        id: string
        score: number
        suggestAmount: number
        reasoning: string
    }[]
    comment: string
}


export type WishSpendPlanningAdvisor = {
    amount: number
    description: string
}

export type GoalPlanningAdvisor = {
    id: string,
    description: string
    target: number
    currentBalance: number
    score: number
    desirValue: number
    importance: number
    wishDueDate?: string
}

export type WishGoalTargetPlanningAdvisor = {
    goalId: string
    amount: number
}

export type AgentPlanningAdvisorInput = {
    comment: string
    currentAmountInInvestissment: number
    currentAmountInSaving: number
    percentForSavingAndInvestissment: number
    income: number
    amountToAllocate: number
    whishGoalTarget: WishGoalTargetPlanningAdvisor[]
    goals: GoalPlanningAdvisor[]
    wishSpends: WishSpendPlanningAdvisor[]
}

export default interface IAgentPlanningAdvisor extends IAgent<AgentPlanningAdvisorInput, AgentPlanningAdvisorOutput> {}
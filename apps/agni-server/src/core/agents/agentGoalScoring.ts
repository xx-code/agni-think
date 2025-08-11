import { IAgent } from "./interface";

export interface AgentGoalScoringOutput {
    goals: {
        id: string
        score: number,
        suggestAmount: number
    } []
}

export type InputSaveGoalAgent = {
    id: string
    currentBalance: number
    target: number
    desirValue: number
    importance: number
    dueDate?: Date
}

export interface AgentGoalScoringInput {
    allocateAmount: number
    saveGoals: InputSaveGoalAgent[]
}

export default interface IAgentScoringGoal extends IAgent<AgentGoalScoringInput, AgentGoalScoringOutput> {}
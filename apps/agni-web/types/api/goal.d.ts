import type { QueryFilterRequest } from "."

export type CreateGoalRequest = {
    title: string
    description: string
    targetAmount: number
    targetSourceId: string
    targetDate: string
    status: number
    type: string
    repeater?: {
        period: string
        interval: number
    }
}

export type UpdateGoalRequest = {
    title?: string
    description?: string
    targetAmount?: number
    targetDate?: string
    status?: number
    repeater?: {
        period: string
        interval: number
    }
}

export type GoalResponse = {
    id: string
    title: string
    description: string
    targetAmount: number
    targetSourceId: string
    dueDate: string
    createdDate: string
    status: number
    type: string
    repeater?: {
        periodType: string
        interval: number
    }
    evaluation: {
        currentBalance: number
        progressPercentage: number
    } 
}

export type GoalQueryFilterRequest = QueryFilterRequest & {
    sourceId?: string
    status?: string
    type?: string
}
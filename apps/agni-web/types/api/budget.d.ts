import type { QueryFilterRequest } from "."

export type GetBudgetResponse = {
    id: string,
    title: string,
    target: number,
    currentBalance: number
    dueDate: Date
    repeater?: {
        period: string
        interval: number
    }
} 

export type CreateBudgetRequest = {
    title: string;
    target: number;
    schedule: {
        repeater?: {
            period: string
            interval: number 
        }
        dueDate: string
    } 
} 

export type UpdateBudgetRequest = {
    title?: string;
    target?: number;
    schedule?: {
        repeater?: {
            period: string
            interval: number
        }
        dueDate: string
    }  
}

export type BudgetQueryFilterRequest = QueryFilterRequest & {
    periodTypes?: string[]
}
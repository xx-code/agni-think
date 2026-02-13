import type { QueryFilterRequest } from "."

export type GetSavingGoalResponse = {
    id: string,
    title: string,
    description: string,
    target: number,
    balance: number
    desirValue: number
    importance: number
    wishDueDate?: Date
    accountId?: string
    items: {
        title: string
        url: string
        price: number
    }[] 
}

export type CreateSavingGoalRequest = {
    target: number;
    title: string;
    accountId?: string
    description: string
    desirValue: number
    importance: number
    wishDueDate?: string
    items: {
        title: string
        price: number
        url: string
    }[]
}

export type UpdateSavingGoalRequest = {
    target?: number
    title?: string
    accountId?: string
    description?: string
    desirValue?: number
    importance?: number
    wishDueDate?: string
    items?: {
        title: string,
        url: string,
        price: number
    }[]
}

export type UpgradeSavingGoalRequest = {
    accountId: string
    amount: number
}

export type DeleteSavingGoalRequest = {
    accountId: string
}

export type QueryFilterSavingGoalRequest = QueryFilterRequest & {
}
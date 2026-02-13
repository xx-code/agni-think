import type { CalendarDate } from "@internationalized/date"

export type EditSaveGoalType = {
    title: string,
    accountId?: string,
    description: string,
    target: number,
    desirValue: number
    importance: number
    wishDueDate?: CalendarDate
}

export type SaveGoalType = {
    id: string,
    accountId?: string
    title: string,
    description: string,
    target: number,
    balance: number
    desirValue: number
    importance: number
    wishDueDate?: Date
    items: {
        title: string
        url: string
        price: number
    }[] 
}

export type EditUpdateAmountSaveGoalType = {
    accountId: string
    amount: number
}
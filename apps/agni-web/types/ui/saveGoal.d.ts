import type { CalendarDate } from "@internationalized/date"

export type EditSaveGoalType = {
    title: string,
    description: string,
    target: number,
    desirValue: number
    importance: number
    wishDueDate?: CalendarDate
}

export type SaveGoalType = {
    id: string,
    title: string,
    description: string,
    target: number,
    balance: number
    desirValue: number
    importance: number
    wishDueDate?: Date
    items: {
        title: string
        link: string
        price: number
        htmlToTrack: string
    }[] 
}

export type EditUpdateAmountSaveGoalType = {
    accountId: string
    amount: number
}
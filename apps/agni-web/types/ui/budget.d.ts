import type { Calendar, CalendarDate } from "@internationalized/date"

export type EditBudgetType = {
    title: string,
    target: number, 
    saveGoalIds: string[]
    repeater?: {
        period: string
        interval: number
    }
    dueDate: CalendarDate
}

export type BudgetType = {
    id: string,
    title: string,
    target: number,
    realTarget: number,
    saveGoalTarget: number
    saveGoalIds: string[]
    currentBalance: number
    repeater?: {
        period: string
        interval: number
    }
    dueDate: Date
}
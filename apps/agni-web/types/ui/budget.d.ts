import type { Calendar, CalendarDate } from "@internationalized/date"

export type EditBudgetType = {
    title: string,
    target: number, 
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
    currentBalance: number
    dueDate: Date
    repeater?: {
        period: string
        interval: number
    }
}
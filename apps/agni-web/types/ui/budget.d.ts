import type { Calendar, CalendarDate } from "@internationalized/date"
import type { BudgetQueryFilterRequest } from "../api/budget"

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

export type BudgetCard = {
    id: string 
    title: string
    target: number
    balance: number
    dueDate: Date
    repeater?: {
        period: string
        interval: number
    }
}

export type BudgetFilter = Omit<BudgetQueryFilterRequest, 'periodTypes'> & { periodTypes?: {id: string, label: string}[]}

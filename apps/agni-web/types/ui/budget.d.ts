import type { Calendar, CalendarDate } from "@internationalized/date"

export type EditBudgetType = {
    title: string,
    target: number, 
    period: string
    saveGoalIds: string[]
    periodTime?: number
    startDate: CalendarDate
    endDate?: CalendarDate

}

export type BudgetType = {
    id: string,
    title: string,
    target: number,
    realTarget: number,
    saveGoalTarget: number
    saveGoalIds: string[]
    period: string
    periodTime?: number
    currentBalance: number
    startDate: Date
    updateDate: Date
    endDate?: Date
}
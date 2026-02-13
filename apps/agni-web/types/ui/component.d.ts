import type { CalendarDate } from "@internationalized/date"

export type FormFilterTransaction = {
    accountIds: string[]
    categoryIds: string[]
    tagIds: string[]
    budgetIds: string[]
    types?: string[]
    status?: string
    minPrice?: number
    maxPrice?: number
    dateStart?: CalendarDate
    dateEnd?: CalendarDate
}
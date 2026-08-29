import type { CalendarDate } from "@internationalized/date"

export type FormFilterTransaction = {
    tagIds: string[]
    budgetIds: string[]
    types?: string[]
    status?: string
    minPrice?: number
    maxPrice?: number
}
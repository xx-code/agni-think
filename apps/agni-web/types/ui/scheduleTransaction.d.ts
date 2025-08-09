import type { CalendarDate } from "@internationalized/date"

export type EditScheduleTransactionType = {
    name: string
    accountId: string
    categoryId: string
    tagIds: string[]
    type: string
    amount: number
    dateStart: CalendarDate
    period: string
    periodTime?: number
    dateEnd?: CalendarDate
}

export type TableScheduleTransactionType = {
    id: string
    name: string
    category: {
        id: string,
        icon: string,
        title: string,
        color: string,
    },
    tags: {
        id: string
        value: string
        color: string
    }[]
    type: string
    amount: number
    isPause: boolean
    dateStart: string
    dateUpdate: string
    dateEnd?: string
}

export type ScheduleTransactionType = {
    id: string
    name: string
    accountId: string
    categoryId: string
    tagIds: string[]
    type: string
    amount: number
    isPause: boolean
    dateStart: string
    period: string
    dateUpdate: string
    periodTime?: number
    dateEnd?: string
}


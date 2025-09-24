import type { CalendarDate } from "@internationalized/date"

export type EditScheduleTransactionType = {
    name: string
    accountId: string
    categoryId: string
    tagIds: string[]
    type: string
    amount: number
    isFreeze: boolean
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
    isFreeze: boolean
    dateStart: Date
    dateUpdate: Date
    dateEnd?: Date
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
    dateStart: Date
    isFreeze: boolean
    period: string
    dateUpdate: Date
    periodTime?: number
    dateEnd?: Date
}


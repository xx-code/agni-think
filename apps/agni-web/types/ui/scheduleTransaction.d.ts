import type { CalendarDate } from "@internationalized/date"

export type EditScheduleTransactionType = {
    name: string
    accountId: string
    categoryId?: string
    tagIds: string[]
    type: string
    amount: number
    isFreeze: boolean
    repeater?: {
        period: string
        interval: number
    }
    dueDate: CalendarDate
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
    repeater?: {
        period: string
        interval: number
    }
    dueDate: Date
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
    isFreeze: boolean
    repeater?: {
        period: string
        interval: number
    }
    dueDate: Date
}


import type { CalendarDate } from "@internationalized/date"
import type { GetScheduleInvoiceResponse } from "../api/scheduleTransaction"

export type EditScheduleInvoiceType = {
    name: string
    accountId: string
    categoryId?: string
    tagIds: string[]
    type?: string
    amount: number
    isFreeze: boolean
    dueDate: CalendarDate
    endDate?: CalendarDate
    freezeEndDate?: CalendarDate
    repeater?: {
        periodType: string
        interval: number
    }
    freezeRepeater?: {
        periodType: string
        interval: number
    }
}

export type TableScheduleInvoiceType = {
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

export type ScheduleInvoiceType = Omit<GetScheduleInvoiceResponse, 'dueDate' | 'endDate' | 'freezeEndDate' | 'freeze'> & {
    dueDate: Date
    endDate?: Date
    freezeEndDate?: Date
    isFreeze: boolean
}


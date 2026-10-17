import type { CalendarDate } from "@internationalized/date"
import type { GetScheduleInvoiceResponse } from "../api/scheduleTransaction"

export type EditScheduleInvoice = {
    name: string
    accountId: string
    categoryId?: string
    tagIds: string[]
    type: string
    invoiceType?: string
    amount: number
    isFreeze: boolean
    dueDate: CalendarDate
    endDate?: CalendarDate
    freezeEndDate?: CalendarDate
    repeater?: {
        period: string
        interval: number
    }
    freezeRepeater?: {
        period: string
        interval: number
    }
}

export type TableScheduleInvoice = {
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

export type ScheduleInvoice = Omit<GetScheduleInvoiceResponse, 'dueDate' | 'endDate' | 'freezeEndDate' | 'editable' | 'pause'> & {
    dueDate: Date
    endDate?: Date
    isPause: boolean
    freezeEndDate?: Date
    isFreeze: boolean
    isEditable: boolean
}


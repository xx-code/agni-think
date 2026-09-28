import type { CalendarDate } from "@internationalized/date"

export type EditIncomeSourceType = {
    title: string
    type: string
    payFrequencyType: string
    reliabilityLevel: number
    taxRate: number
    otherRate: number
    startDate: CalendarDate
    endDate?: CalendarDate
    invoiceIncomeCategoryId?: string
    linkedAccountId?: string
    annualGrossAmount?: number
}

export type IncomeSourceType = {
    id: string
    title: string
    type: string
    reliabilityLevel: number
    startDate: Date
    taxRate: number
    otherRate: number
    payFrequencyType: string
    estimatedFutureOccurrences: number
    invoiceIncomeCategoryId?: string
    estimateNextNetAmount?: number
    linkedAccountId?: string
    annualGrossAmount?: number
    endDate?: Date
}
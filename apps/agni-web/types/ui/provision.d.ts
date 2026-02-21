import type { CalendarDate } from "@internationalized/date"

export type EditProvisionType = {
    title: string
    initialCost: number
    expectedLifespanMonth: number
    acquisitionDate: CalendarDate
    residualValue: number
}

export type ProvisionType = {
    id: string,
    title: string,
    initialCost: number,
    acquisitionDate: Date,
    expectedLifespanMonth: number,
    residualValue: number
}
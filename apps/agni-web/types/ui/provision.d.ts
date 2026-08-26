import type { CalendarDate } from "@internationalized/date"
import type { GetProvisionResponse } from "../api/provision"

export type EditProvisionType = {
    title: string
    initialCost: number
    expectedLifespanMonth: number
    acquisitionDate: CalendarDate
    residualValue: number
}


export type Provision = Omit<GetProvisionResponse, 'acquisitionDate', 'nextPaymentDate'> & {
    acquisitionDate: Date, nextPaymentDate?: Date
} 
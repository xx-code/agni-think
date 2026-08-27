import type { CalendarDate } from "@internationalized/date"
import type { GetProvisionInvoiceResponse, GetProvisionResponse, ProvisionScheduleInvoiceRequest } from "../api/provision"
import type { DepreciateType, ProvisionType } from "../constants/provision"
import type { GetScheduleInvoiceResponse } from "../api/scheduleTransaction"

export type ProvisionDepreciationCriteria = {
    title: string
    description: string
    type: DepreciateType
    value: number
    monthRange: number
}

export type ProvisionScheduleInvoiceForm = {
    accountId: string
    categoryId: string
    tagIds: string[]
    budgetIds: string[]
    paymentPeriod: string
    paymentInterval: number
}

export type EditProvision = {
    title: string
    costHT: number
    costTTC: number
    expectedLifespanMonth: number
    acquisitionDate: CalendarDate
    isPatrimony: boolean
    floorValue: number
    interestLoan: number
    loanMonth: number
    type: ProvisionType
    depreciationCriteria: ProvisionDepreciationCriteria[]
    scheduleInvoice?: ProvisionScheduleInvoiceForm
}

export type Provision = Omit<GetProvisionResponse, 'acquisitionDate' | 'type' | 'patrimony' | 'depreciationCriteria'> & {
    acquisitionDate: Date
    type: ProvisionType
    isPatrimony: boolean
    depreciationCriteria: ProvisionDepreciationCriteria[]
}

export type ProvisionCard = Provision & {
    remainingMonths: number
    amortizationPercent: number
}

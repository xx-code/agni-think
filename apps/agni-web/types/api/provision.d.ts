export type ScheduelProvisionRequest = {
    dueDate: string
    repeater?: {
        interval: number
        period: string
    }
}

export type ProvisionDepreciateCriteriaRequest = {
    title: string
    description: string
    type: string
    value: number
    monthRange: number
}

export type ProvisionScheduleInvoiceRequest = {
    invoiceAccountId: string
    invoiceCategoryId: string
    scheduler: ScheduelProvisionRequest
    endDate: string
    tagIds: string[]
    budgetIds: string[]
}

export type CreateProvisionRequest = {
    title: string,
    initialCost: number,
    acquisitionDate: string,
    expectedLifespanMonth: number,
    isPatrimony: boolean
    depreciationCriteria: ProvisionDepreciateCriteriaRequest[]
    scheduleInvoice?: ProvisionScheduleInvoiceRequest 
    floorValue: number
    interestLoan: number
    loanMonth: number
}

export type UpdateProvisionRequest = {
    title?: string,
    initialCost?: number,
    acquisitionDate?: string,
    expectedLifespanMonth?: number,
    isPatrimony?: boolean
    depreciationCriteria?: ProvisionDepreciateCriteriaRequest[]
    scheduleInvoice?: ProvisionScheduleInvoiceRequest 
    floorValule?: number
    interestLoan?: number
    loanMonth?: number
}

export type GetProvisionResponse = {
    id: string,
    title: string,
    initialCost: number,
    totalCost: number
    acquisitionDate: string,
    expectedLifespanMonth: number,
    costByMonth: number
    monthlyPayment: number
    residualValue: number
    nextPaymentDate?: string
    nextPaymnetAmount?: number
}

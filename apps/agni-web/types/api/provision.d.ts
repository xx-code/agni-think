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
    tagIds: string[]
    budgetIds: string[]
    paymentPeriod: string
    paymentInterval: number
}

export type CreateProvisionRequest = {
    title: string,
    costHT: number,
    costTTC: number,
    acquisitionDate: string,
    expectedLifespanMonth: number,
    type: string
    isPatrimony: boolean
    depreciationCriteria: ProvisionDepreciateCriteriaRequest[]
    scheduleInvoice?: ProvisionScheduleInvoiceRequest 
    floorValue: number
    interestLoan: number
    loanMonth: number
}

export type UpdateProvisionRequest = {
    title?: string,
    costHT?: number,
    costTTC?: number,
    acquisitionDate?: string,
    expectedLifespanMonth?: number,
    type: string
    isPatrimony?: boolean
    depreciationCriteria?: ProvisionDepreciateCriteriaRequest[]
    scheduleInvoice?: ProvisionScheduleInvoiceRequest 
    floorValue?: number
    interestLoan?: number
    loanMonth?: number
}

export type GetProvisionInvoiceResponse = {
    accountId: string
    categoryId: string
    paymentPeriod: string
    paymentInterval: number
    nextPaymentDate: string
    tagIds: string[]
    budgetIds: string[]
}

export type GetProvisionDepreciateCriteriaResponse = {
    title: string
    description: string
    type: string
    value: number
    monthRange: number
}

export type GetProvisionResponse = {
    id: string,
    title: string,
    costHT: number,
    costTTC: number,
    totalCost: number
    acquisitionDate: string,
    expectedLifespanMonth: number,
    costByMonth: number
    monthlyPayment: number
    residualValue: number
    type: string
    patrimony: boolean
    floorValue: number
    interestLoan: number
    loanMonth: number
    depreciationCriteria: GetProvisionDepreciateCriteriaResponse[]
    scheduleInvoice?: GetProvisionInvoiceResponse
}

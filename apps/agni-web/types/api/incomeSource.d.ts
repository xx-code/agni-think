export type CreateIncomeSourceRequest = {
    title: string,
    type: string,
    payFrequencyType: string,
    reliabilityLevel: number,
    taxRate: number,
    otherRate: number,
    startDate: string,
    endDate?: string,
    linkedAccountId?: string,
    annualGrossAmount?: number
}

export type UpdateIncomeSourceRequest = {
    title?: string,
    type?: string,
    payFrequencyType?: string,
    reliabilityLevel?: number,
    taxRate?: number,
    otherRate?: number,
    startDate?: string,
    endDate?: string,
    linkedAccountId?: string,
    annualGrossAmount?: number
}

export type GetIncomeSourceResponse = {
    id: string
    title: string
    payFrequencyType: string
    type: string,
    reliabilityLevel: number
    taxRate: number
    otherRate: number
    startDate: string
    estimatedFutureOccurrences: number
    estimateNextNetAmount?: number
    linkedAccountId?: string
    annualGrossAmount?: number
    endDate?: string
}

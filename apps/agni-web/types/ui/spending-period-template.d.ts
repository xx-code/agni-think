import type { GetSpendingPeriodTemplateResponse } from "../api/spending-period-template"

export type SpendingPeriodTemplate = Omit<GetSpendingPeriodTemplateResponse, 'startDate' | 'endDate' | 'isActive'> & {
    startDate: Date
    endDate?: Date
    isActive: boolean
}

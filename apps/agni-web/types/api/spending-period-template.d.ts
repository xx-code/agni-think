export type CreateSpendingPeriodTemplateRequest = {
    recurrence: {
        period: string
        interval: number
    }
    targetBudgetIds: string[]
    startDate: string
    endDate?: string
}

export type UpdateSpendingPeriodTemplateRequest = {
    startDate?: string
    endDate?: string
    isActive?: boolean
    targetBudgetIds?: string[]
    recurrence?: {
        period: string
        interval: number
    }
}

export type GetSpendingPeriodTemplateResponse = {
    id: string
    recurrence: {
        period: string
        interval: number
    }
    active: boolean
    budgets: {
        id: string
        title: string
    }[]
    startDate: string
    endDate?: string
}

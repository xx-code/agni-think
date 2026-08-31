export type GetScheduleInvoiceResponse = {
    id: string
    name: string
    accountId: string
    categoryId: string
    tagIds: string[]
    type: string
    amount: number
    isPause: boolean
    freeze: boolean
    dueDate: string
    repeater?: {
        periodType: string
        interval: number
    }
    endDate?: string,
    freezeEndDate?: string
    freezeRepeater?: {
        periodType: string
        interval: number
    }
}

export type CreateScheduleInvoiceRequest = {
    name: string
    accountId: string
    amount: number
    categoryId?: string
    description: string 
    tagIds: string[]
    type?: string
    isFreeze: boolean
    schedule: {
        repeater?: {
            period: string
            interval: number
        }
        dueDate: string
    } 
    endDate?: string
    freezeSchedule?: {
        repeater?: {
            period: string
            interval: number
        }
        dueDate: string
    }
}

export type UpdateScheduleInvoiceRequest = {
    name?: string
    accountId?: string
    amount?: number
    categoryId?: string
    tagIds?: string[]
    type?: string
    isPause?: boolean
    schedule?: {
        repeater?: {
            period: string
            interval: number
        }
        dueDate: string
    }
    endDate?: string
    freezeSchedule?: {
        repeater?: {
            period: string
            interval: number
        }
        dueDate: string
    }
}
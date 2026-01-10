export type GetScheduleTransactionResponse = {
    id: string
    name: string
    accountId: string
    categoryId: string
    tagIds: string[]
    type: string
    amount: number
    isPause: boolean
    isFreeze: boolean
    dueDate: Date
    repeater?: {
        period: string
        interval: number
    }
}

export type GetAllScheduleTransactionsResponse = {
    id: string
    name: string
    accountId: string
    categoryId: string
    tagIds: string[]
    type: string
    amount: number,
    isPause: boolean
    isFreeze: boolean
    dueDate: Date
    repeater?: {
        period: string
        interval: number
    }
}

export type CreateScheduleTransactionRequest = {
    name: string
    accountId: string
    amount: number
    categoryId?: string
    description: string 
    tagIds: string[]
    type: string
    isFreeze: boolean
    schedule: {
        repeater?: {
            period: string
            interval: numbrer
        }
        dueDate: string
    } 
}

export type UpdateScheduleTransactionRequest = {
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
            interval: numbrer
        }
        dueDate: string
    }
}
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
    dateStart: Date
    period: string
    dateUpdate: Date
    periodTime?: number
    dateEnd?: Date
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
    dateStart: Date
    period: string
    dateUpdate: Date
    periodTime?: number
    dateEnd?: Date
}

export type CreateScheduleTransactionRequest = {
    name: string
    accountId: string
    amount: number
    categoryId: string
    description: string 
    tagIds: string[]
    type: string
    isFreeze: boolean
    schedule: {
        period: string,
        periodTime?: number
        dateStart: string
        dateEnd?: string
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
        period: string,
        periodTime?: number
        dateStart: string
        dateEnd?: string
    }
}
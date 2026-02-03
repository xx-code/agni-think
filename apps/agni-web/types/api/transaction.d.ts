export type GetBalanceResponse = {
    balance: number
    income: number
    spend: number
}

export type RecordResponse = {
    id: string
    amount: number
    categoryId: string
    description: string
    tagRefs: string[]
    budgetRefs: string[]
}

export type TransactionDeductionResponse = {
    id: string
    amount: number
}

export type GetTransactionResponse = {
    id: string
    accountId: string
    status: string
    type: string
    mouvement: string
    subTotalAmount: number
    totalAmount: number
    date: Date
    records: RecordResponse[]
    deductions: TransactionDeductionResponse[]
}

export type GetAllTransactionResponse = {
    id: string
    accountId: string
    status: string
    type: string
    mouvement: string
    subTotalAmount: number
    totalAmount: number
    date: Date
    records: RecordResponse[]
    deductions: TransactionDeductionResponse[]
}

export type FilterBalanceTransactionQuery = {
    accountFilterIds?: string[],
    tagFilterIds?: string[],
    categoryFilterIds?: string[],
    budgetFilterIds?: string[],
    dateStart?: Date,
    dateEnd?: Date,
    types?: string,
    minPrice?: number,
    maxPrice?: number
}

export type FilterTransactionQuery = {
    offset: number,
    limit: number,
    sortBy?: string,
    sortSense?: string,
    accountFilterIds?: string[],
    categoryFilterIds?: string[],
    budgetFilterIds?: string[],
    tagFilterIds?: string[],
    dateStart?: Date,
    dateEnd?: Date,
    types?: string[],
    minPrice?: number,
    maxPrice?: number,
    status?: string,
    isFreeze?: boolean
}

export type CreateTransactionRequest = {
    accountId: string
    status: string
    date: string
    type: string
    mouvement: string
    currencyId?: string
    records: {
        amount: number
        categoryId: string
        description: string
        tagIds: string[]
        budgetIds: string[]
    }[]
    deductions: {
        deductionId: string
        amount: number
    }[]
}

export type UpdateTransactionRequest = {
    id: string;
    accountId?: string
    mouvement?: string
    date?: string
    type?: string
    currencyId?: string
    removeRecordIds: string[]
    addRecords: {
        amount: number
        categoryId: string
        description: string
        tagIds: string[]
        budgetIds: string[]
    }[]
    deductions: {
        deductionId: string
        amount: number
    }[]
}

export type TransfertTransactionRequest = {
    accountIdFrom: string;
    accountIdTo: string;
    date: string;
    amount: number;
}

export type FreezeTransactionRequest = {
    accountId: string,
    title: string
    amount: number,
    endDate: string
}
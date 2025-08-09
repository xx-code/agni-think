export type GetBalanceResponse = {
    balance: number
}

export type GetTransactionResponse = {
    transactionId: string
    accountId: string
    amount: number
    date: string
    description: string
    recordType: string
    type: string
    status: string
    categoryId: string
    tagRefs: string[]
    budgets: string[]
}

export type GetAllTransactionResponse = {
    transactionId: string
    accountId: string
    amount: number
    date: string
    description: string
    recordType: string
    type: string
    status: string
    categoryId: string
    tagRefs: string[]
    budgets: string[]
}

export type FilterBalanceTransactionQuery = {
    accountFilterIds?: string[],
    tagFilterIds?: string[],
    categoryFilterIds?: string[],
    budgetFilterIds?: string[],
    dateStart?: string,
    dateEnd?: string,
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
    dateStart?: string,
    dateEnd?: string,
    types?: string[],
    minPrice?: number,
    maxPrice?: number,
    isFreeze?: boolean
}

export type CreateTransactionRequest = {
    accountId: string
    amount: number
    categoryId: string
    description: string
    date: string
    tagIds: string[]
    budgetIds: string[]
    type: string
}

export type UpdateTransactionRequest = {
    accountId?: string
    tagIds?: string[]
    budgetIds?: string[]
    categoryId?: string
    type?: string
    description?: string
    date?: string
    amount?: number
}

export type TransfertTransactionRequest = {
    accountIdFrom: string;
    accountIdTo: string;
    date: string;
    amount: number;
}

export type FreezeTransactionRequest = {
    accountId: string,
    amount: number,
    endDate: string
}
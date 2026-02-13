export type GetBalanceResponse = {
    balance: number
    income: number
    spend: number
}

export type TransactionResponse = {
    id: string
    amount: number
    categoryId: string
    description: string
    tagIds: string[]
    budgetIds: string[]
}

export type InvoiceDeductionResponse = {
    id: string
    amount: number
}

export type GetInvoiceResponse = {
    id: string
    accountId: string
    status: string
    type: string
    mouvement: string
    subTotal: number
    total: number
    date: Date
    transactions: TransactionResponse[]
    deductions: InvoiceDeductionResponse[]
}

export type QueryBalanceByPeriod = {
    period: string
    interval: number
    dateFrom: string,
    dateTo?: string,
    accountIds?: string[],
    categoryIds?: string[],
    mouvement?: string,
    budgetIds?: string[],
    tagIds?: string[],
    types?: string[],
    minAmount?: number,
    maxAmount?: number,
    status?: string,
    isFreeze?: boolean
}

export type QueryInvoice = {
    accountIds?: string[],
    categoryIds?: string[],
    budgetIds?: string[],
    tagIds?: string[],
    mouvement?: string,
    startDate?: string,
    endDate?: string,
    types?: string[],
    minAmount?: number,
    maxAmount?: number,
    status?: string,
    isFreeze?: boolean
}

export type CreateInvoiceRequest = {
    accountId: string
    status: string
    date: string
    type: string
    mouvement: string
    currencyId?: string
    transactions: {
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

export type UpdateInvoiceRequest = {
    accountId?: string
    mouvement?: string
    date?: string
    type?: string
    currencyId?: string
    removeTransactionIds: string[]
    addTransactions: {
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

export type TransferInvoiceRequest = {
    accountIdFrom: string;
    accountIdTo: string;
    date: string;
    amount: number;
}

export type FreezeInvoiceRequest = {
    accountId: string,
    title: string
    amount: number
    endDate: string
    status: string
}
export type GetBalanceResponse = {
    balance: number
}

export type FilterBalanceTransactionQuery = {
    accountIds?: string[],
    tagIds?: string[],
    categoryIds?: string[],
    budgetIds?: string[],
    dateStart?: string,
    dateEnd?: string,
    types?: string,
    minPrice?: number,
    maxPrice?: number
}

export type FilterTransactionQuery = {
    offset?: number,
    limit?: number,
    sortBy?: string,
    sortSense?: string,
    accountFilter?: string[],
    categoryFilter?: string[],
    budgetFilter?: string[],
    tagFilter?: string[],
    dateStart?: string,
    dateEnd?: string,
    types?: string[],
    minPrice?: string,
    maxPrice?: string
}
export type FormFilterTransaction = {
    accountIds: string[]
    categoryIds: string[]
    tagIds: string[]
    budgetIds: string[]
    status: string[]
    minPrice?: number
    maxPrice?: number
    dateStart?: string
    dateEnd?: string
}
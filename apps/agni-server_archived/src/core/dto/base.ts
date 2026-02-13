export type CreatedDto = {
    newId: string
}

export type ListDto<TOut> = {
    items: TOut[],
    totals: number 
}

export interface QueryFilter {
    queryAll?: boolean
    offset: number
    limit: number
    sortBy?: string
    sortSense?: string
}
export type CreatedDto = {
    newId: string
}

export type ListDto<TOut> = {
    items: TOut[],
    totals: number 
}

export type QueryAllFetch = {
    queryAll?: boolean
    offset: number
    limit: number
}
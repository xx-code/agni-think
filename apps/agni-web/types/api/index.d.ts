export type CreatedRequest =  {
    newId: string
};

export type ListResponse<T> = {
    items: T[]
    totals: number
}

export type QueryFilterRequest = {
    offset: number
    limit: number
    queryAll?: boolean
    sortBy?: {
        by: string,
        ascending: boolean
    }
}
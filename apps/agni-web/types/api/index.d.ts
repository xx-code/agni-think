export type CreatedRequest =  {
    newId: string
};

export type ListResponse<T> = {
    items: T[]
    total: number
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

export type ErrorResponse = {
    status: number
    error: string
    message: string
    errors: Map<string, string?>
    timestamp: number
}

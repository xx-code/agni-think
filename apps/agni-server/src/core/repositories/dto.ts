export type RepositoryListResult<T> = {
    items: T[],
    total: number
} 

export type QueryFilterAllRepository = {
    offset: number    
    limit: number
    sort?: SortBy
    queryAll?: boolean
}

export type SortBy = {
    sortBy: string,
    asc: boolean
}
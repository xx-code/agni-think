export type CreatedRequest =  {
    newId: string
};

export type ListResponse<T> = {
    items: T[]
    totals: number
}
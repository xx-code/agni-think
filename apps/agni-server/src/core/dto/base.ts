export type CreatedDto = {
    newId: string
}

export type ListDto<TOut> = {
    items: TOut[],
    totals: number 
}
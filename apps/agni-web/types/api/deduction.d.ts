export type GetDeductionResponse = {
    id: string
    title: string
    description: string
    base: string
    mode: string
} 


export type RequestCreateDeduction = {
    title: string
    description: string
    mode: string
    base: string
}

export type RequestUpdateDeduction = {
    title?: string
    description?: string
}

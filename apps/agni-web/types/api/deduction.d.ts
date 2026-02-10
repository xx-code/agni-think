export type GetDeductionResponse = {
    id: string
    title: string
    description: string
    base: number
    mode: number
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

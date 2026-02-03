export type GetDeductionTypeResponse = {
    id: string
    title: string
    description: string
    base: string
    mode: string
} 

export type GetAllDeductionTypeResponse = {
    id: string
    title: string
    description: string
    base: string
    mode: string
}

export type GetDeductionResponse = {
    id: string
    typeId: string
    typeName: string
    rate: number
} 

export type GetAllDeductionResponse = {
    id: string
    typeId: string
    typeName: string 
    rate: number
}

export type RequestCreateDeduction = {
    typeId: string
    base: string 
}

export type RequestUpdateDeduction = {
    id: string
    rate?: number
}

export type RequestCreateDeductionType = {
    title: string
    description: string
    mode: string
    base: string
}

export type RequestUpdateDeductionType = {
    id: string
    title?: string
    description?: string
}

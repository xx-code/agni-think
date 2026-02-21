export type CreateFinancePrincipleRequest = {
    name: string
    description: string
    targetType: string
    strictness: number
    logicRules?: string
}

export type UpdateFinancePrincipleRequest = {
    name?: string
    description?: string
    targetType?: string
    strictness?: number
    logicRules?: string
}

export type GetFinancePrincipleResponse = {
    id: string,
    name: string
    description: string
    targetType: string
    strictness: number
    logicRules?: string
}
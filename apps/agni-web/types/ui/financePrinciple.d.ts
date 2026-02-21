export type EditFinancePrincipleType = {
    name: string
    description: string
    targetType: string
    strictness: number
    logicRules?: string
}

export type FinancePrincipleType = {
    id: string
    name: string
    description: string
    targetType: string
    strictness: number
    logicRules?: string
}

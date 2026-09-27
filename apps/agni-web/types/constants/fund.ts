export enum FundType {
    Emergency = "Emergency",
    Amortization = "Amortization",
    SinkingFund = "SinkingFund",
    ProjectTarget = "ProjectTarget",
    Opportunity = "Opportunity",
    SavingsGeneral = "SavingsGeneral"
}


export const FUND_TYPE_CONFIG: Record<FundType, string> = { 
    [FundType.Emergency]: 'Emergency', 
    [FundType.Amortization]: 'Amortization', 
    [FundType.SinkingFund]: 'SinkingFund', 
    [FundType.ProjectTarget]: 'ProjectTarget', 
    [FundType.Opportunity]: 'Opportunity', 
    [FundType.SavingsGeneral]: 'SavingsGeneral'
}

export const FUND_TYPE_LABEL: Record<FundType, string> = { 
    [FundType.Emergency]: "Fonds d'urgence", 
    [FundType.Amortization]: 'Amortissement', 
    [FundType.SinkingFund]: "Fonds d'amortissement", 
    [FundType.ProjectTarget]: 'Objectif de projet', 
    [FundType.Opportunity]: 'Opportunité', 
    [FundType.SavingsGeneral]: 'Épargne générale'
}

export const FUND_TYPE_CLASS: Record<FundType, string> = { 
    [FundType.Emergency]: 'text-red-700 bg-red-100', 
    [FundType.Amortization]: 'text-orange-700 bg-orange-100', 
    [FundType.SinkingFund]: 'text-amber-700 bg-amber-100', 
    [FundType.ProjectTarget]: 'text-blue-700 bg-blue-100', 
    [FundType.Opportunity]: 'text-emerald-700 bg-emerald-100', 
    [FundType.SavingsGeneral]: 'text-primary-700 bg-primary-50'
}

export const FUND_TYPE_ICON: Record<FundType, string> = { 
    [FundType.Emergency]: 'i-lucide-shield-alert', 
    [FundType.Amortization]: 'i-lucide-hammer', 
    [FundType.SinkingFund]: 'i-lucide-piggy-bank', 
    [FundType.ProjectTarget]: 'i-lucide-target', 
    [FundType.Opportunity]: 'i-lucide-trending-up', 
    [FundType.SavingsGeneral]: 'i-lucide-wallet'
}

export const FUND_TYPE_LIST: FundType[] = Object.values(FundType)

export const getLabelFundType = (type?: string): string => {
    if (!type) return 'Non spécifié'
    return FUND_TYPE_LABEL[type as FundType] ?? FUND_TYPE_CONFIG[type as FundType] ?? 'Non spécifié'
}

export const getClassFundType = (type?: string): string => {
    if (!type) return 'text-gray-500 bg-gray-100'
    return FUND_TYPE_CLASS[type as FundType] ?? 'text-gray-500 bg-gray-100'
}

export const getIconFundType = (type?: string): string => {
    if (!type) return 'i-lucide-circle-help'
    return FUND_TYPE_ICON[type as FundType] ?? 'i-lucide-circle-help'
}
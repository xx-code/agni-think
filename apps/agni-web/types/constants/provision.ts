export enum ProvisionType {
    Depreciate="Depreciate",
    DepreciateLoan="DepreciateLoan"
}

export enum DepreciateType {
    DecliningBalance="DecliningBalance",
    StraightLine="StraightLine",
    Fix="Fix",
    FixPercentage="FixPercentage"
}

export const PROVISION_TYPE_CONFIG: Record<ProvisionType, string> = {
    [ProvisionType.Depreciate]: 'Actif Déprécie',
    [ProvisionType.DepreciateLoan]: 'Actif Déprécie avec pret',
}

export const DEPRECIATE_TYPE_CONFIG: Record<DepreciateType, string> = {
    [DepreciateType.DecliningBalance]: 'Amortissement dégressif ',
    [DepreciateType.StraightLine]: 'Linéaire ',
    [DepreciateType.Fix]: 'Fix',
    [DepreciateType.FixPercentage]: 'Fix en pourcentage',
}

export const getLabelProvisionType = (type: string): string => {
    if (!type) return 'Non spécifié'
    return PROVISION_TYPE_CONFIG[type as ProvisionType] ?? 'Non spécifié'
} 

export const getLabelDepreciateType = (type: string): string => {
    if (!type) return 'Non spécifié'
    return DEPRECIATE_TYPE_CONFIG[type as DepreciateType] ?? 'Non spécifié'
} 
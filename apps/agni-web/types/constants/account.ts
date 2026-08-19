export enum AccountType {
    Checking = "Checking",
    CreditCard = "CreditCard",
    Saving = "Saving",
    Business = "Business",
    Broking = "Broking"
}

export const ACCOUNT_TYPE_CONFIG: Record<AccountType, { label: string; order: number, icon: string }> = {
    [AccountType.Checking]: { label: "Compte courant", order: 0, icon: 'i-lucide-landmark'},
    [AccountType.CreditCard]: { label: "Carte de crédit", order: 1, icon: 'i-lucide-credit-card' },
    [AccountType.Saving]: { label: "Compte d'épargne", order: 2, icon: 'i-lucide-credit-card' },
    [AccountType.Business]: { label: "Compte professionnel", order: 3, icon: 'i-lucide-briefcase-business' },
    [AccountType.Broking]: { label: "Investissement", order: 4, icon: 'i-lucide-chart-candlestick' },
}

export const getLabelAccountType = (type?: string): string => {
    if (!type) return 'Non spécifié'
    return ACCOUNT_TYPE_CONFIG[type as AccountType]?.label ?? 'Non spécifié'
}

export const getOrderAccountType = (type?: string): number => {
    if (!type) return 99
    return ACCOUNT_TYPE_CONFIG[type as AccountType]?.order ?? 99
}

export const getIconAccountType = (type?: string): string => {
    if (!type) return 'i-lucide-landmark'
    return ACCOUNT_TYPE_CONFIG[type as AccountType]?.icon ?? 'i-lucide-landmark'
}
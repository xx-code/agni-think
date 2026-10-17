export enum ScheduleInvoiceType {
    Standard = "Standard",
    Freeze = "Freeze",
    Saving = "Saving"
}

export const SCHEDULE_INVOICE_TYPE_CONFIG: Record<ScheduleInvoiceType, string> = {
    [ScheduleInvoiceType.Standard]: 'Normal',
    [ScheduleInvoiceType.Saving]: 'Saving',
    [ScheduleInvoiceType.Freeze]: 'Freeze',
}

export const getLabelInvoiceType = (type: string): string => {
    if (!type) return 'Non spécifié'
    return SCHEDULE_INVOICE_TYPE_CONFIG[type as ScheduleInvoiceType] ?? 'Non spécifié'
} 
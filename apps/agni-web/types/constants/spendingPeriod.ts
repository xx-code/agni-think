export enum SpendingPeriodType {
    Draft = "Draft",
    ToReview ="ToReview",
    InProgress = "InProgress",
    Complete = "Complete"
}

export function getLabelSpendingPeriodType(state: SpendingPeriodType) {
    if (state === SpendingPeriodType.ToReview)
        return 'En revue'

    if (state === SpendingPeriodType.InProgress)
        return 'En cours'

    if (state === SpendingPeriodType.Complete)
        return 'Terminée'

    return 'Brouillon'
}

export function getClassSpendingPeriodType(state: SpendingPeriodType) {
    if (state === SpendingPeriodType.ToReview)
        return 'bg-blue-50 text-warning-600'

    if (state === SpendingPeriodType.InProgress)
        return 'bg-blue-50 text-blue-600'

    if (state === SpendingPeriodType.Complete)
        return 'bg-emerald-50 text-emerald-600'

    return 'bg-gray-100 text-gray-500'
}

// Once a spending period is in progress it can only be completed or deleted
export function isInProgressSpendingPeriodType(state: SpendingPeriodType) {
    return state === SpendingPeriodType.InProgress || state === SpendingPeriodType.ToReview
}

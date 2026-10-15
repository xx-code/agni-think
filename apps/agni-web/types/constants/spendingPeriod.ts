export enum SpendingPeriodStateType {
    Draft = "Draft",
    ToReview ="ToReview",
    InProgress = "InProgress",
    Complete = "Complete"
}

export function getLabelSpendingPeriodStateType(state: SpendingPeriodStateType) {
    if (state === SpendingPeriodStateType.ToReview)
        return 'En revue'

    if (state === SpendingPeriodStateType.InProgress)
        return 'En cours'

    if (state === SpendingPeriodStateType.Complete)
        return 'Terminée'

    return 'Brouillon'
}

export function getClassSpendingPeriodStateType(state: SpendingPeriodStateType) {
    if (state === SpendingPeriodStateType.ToReview)
        return 'bg-blue-50 text-warning-600'

    if (state === SpendingPeriodStateType.InProgress)
        return 'bg-blue-50 text-blue-600'

    if (state === SpendingPeriodStateType.Complete)
        return 'bg-emerald-50 text-emerald-600'

    return 'bg-gray-100 text-gray-500'
}

export function isInProgressSpendingPeriodStateType(state: SpendingPeriodStateType) {
    return state === SpendingPeriodStateType.InProgress || state === SpendingPeriodStateType.ToReview
}

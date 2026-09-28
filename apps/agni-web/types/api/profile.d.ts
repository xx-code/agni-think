export type UpdateProfileRequest = {
    maxWishlistAmount?: number
    fixSpendPercentage?: number
    varialSpendPercentage?: number
    savingPercentage?: number
    balanceBuffer?: number
}

export type GetProfileResponse = {
    maxWishlistAmount: number
    fixSpendPercentage: number
    varialSpendPercentage: number
    savingPercentage: number
    balanceBuffer: number
}

export type UpdateProfileRequest = {
    maxWishlistAmount?: number
    fixSpendPercentage?: number
    varialSpendPercentage?: number
    savingPercentage?: number
}

export type GetProfileResponse = {
    maxWishlistAmount: number
    fixSpendPercentage: number
    varialSpendPercentage: number
    savingPercentage: number
}

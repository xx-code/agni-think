package usecases.profiles.dto

data class CreateProfileInput(
    val maxWishlistAmount: Double,
    val fixSpendPercentage: Double,
    val varialSpendPercentage: Double,
    val savingPercentage: Double,
    val balanceBuffer: Double
)


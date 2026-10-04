package usecases.currencies.dto

data class CreateCurrencyInput(
    val name: String,
    val symbol: String,
    val locale: String?,
    val rateToBase: Double?,
    val isBase: Boolean?
)

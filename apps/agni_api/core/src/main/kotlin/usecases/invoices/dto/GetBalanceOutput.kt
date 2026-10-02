package usecases.invoices.dto

data class GetBalanceOutput(
    val balance: Double,
    val income: Double,
    val spend: Double
)

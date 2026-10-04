package usecases.accounts.dto

data class GetBufferInfoOutput(
    val baseBufferAmount: Double,
    val currentBalanceBuffer: Double,
    val projectedBuffer: Double,
    val projectedBufferByBalance: Double
)

data class GetTotalBalanceAmountOutput(
    val totalBalance: Double,
    val totalAvailable: Double,
    val totalFreeze: Double,
    val totalLock: Double,
    val totalCreditUtilization: Double,
    val buffer: GetBufferInfoOutput
)
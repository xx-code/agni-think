package usecases.funds.dto

import java.util.UUID

data class DecreaseFundInput(
    val fundId: UUID,
    val accountId: UUID,
    val amount: Double
)

data class IncreaseFundInput(
    val fundId: UUID,
    val accountId: UUID,
    val amount: Double
)

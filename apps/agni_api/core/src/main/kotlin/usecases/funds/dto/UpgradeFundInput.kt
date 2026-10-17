package usecases.funds.dto

import java.util.UUID

data class DecreaseFundInput(
    val userId: UUID,
    val fundId: UUID,
    val accountId: UUID,
    val amount: Double
)

data class IncreaseFundInput(
    val userId: UUID,
    val fundId: UUID,
    val accountId: UUID,
    val amount: Double
)

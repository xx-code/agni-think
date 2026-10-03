package usecases.funds.dto

import domain.enums.FundType
import java.util.UUID

data class CreateFundInput(
    val target: Double,
    val title: String,
    val description: String,
    val type: FundType,
    val accountId: UUID?
)

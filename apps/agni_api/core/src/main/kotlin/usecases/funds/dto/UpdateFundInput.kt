package usecases.funds.dto

import domain.enums.FundType
import java.util.UUID

data class UpdateFundInput(
    val id: UUID,
    val title: String?,
    val target: Double?,
    val description: String?,
    val type: FundType?,
    val accountId: UUID?
    )

package usecases.saving_goals.dto

import domain.enums.FundType
import java.util.UUID

data class UpdateSavingGoalInput(
    val id: UUID,
    val title: String?,
    val target: Double?,
    val description: String?,
    val type: domain.enums.FundType?,
    val accountId: UUID?
    )

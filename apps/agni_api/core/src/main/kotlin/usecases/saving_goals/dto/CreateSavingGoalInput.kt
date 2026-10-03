package usecases.saving_goals.dto

import domain.enums.FundType
import java.util.UUID

data class CreateSavingGoalInput(
    val target: Double,
    val title: String,
    val description: String,
    val type: FundType,
    val accountId: UUID?
)

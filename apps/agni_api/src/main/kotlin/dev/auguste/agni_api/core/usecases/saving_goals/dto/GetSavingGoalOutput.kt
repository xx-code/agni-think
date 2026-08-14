package dev.auguste.agni_api.core.usecases.saving_goals.dto

import java.time.LocalDate
import java.util.UUID

data class FundGoalOutput(
    val id: UUID,
    val title: String,
    val dueDate: LocalDate
)

data class GetSavingGoalOutput(
    val id: UUID,
    val title: String,
    val description: String,
    val target: Double,
    val balance: Double,
    val accountId: UUID?,
    val goals: List<FundGoalOutput>
)

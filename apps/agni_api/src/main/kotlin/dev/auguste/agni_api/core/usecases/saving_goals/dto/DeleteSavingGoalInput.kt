package dev.auguste.agni_api.core.usecases.saving_goals.dto

import java.util.UUID

data class DeleteSavingGoalInput (val savingGoalId: UUID, val accountId: UUID?)
package dev.auguste.agni_api.core.usecases.income_sources.dto

import java.util.UUID

data class DeleteIncomeSourceInput(
    val incomeSourceId: UUID
)

package dev.auguste.agni_api.core.usecases.finance_principles.dto

import java.util.UUID

data class DeleteFinancePrincipleInput(
    val principalId: UUID
)
package dev.auguste.agni_api.core.usecases.finance_principles.dto

import java.util.UUID

data class GetFinancePrincipleOutput(
    val id: UUID,
    val name: String,
    val description: String,
    val targetType: String,
    val strictness: Int,
    val logicRules: String?
)
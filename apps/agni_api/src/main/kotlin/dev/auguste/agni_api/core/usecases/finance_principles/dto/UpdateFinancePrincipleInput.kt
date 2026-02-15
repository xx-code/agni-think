package dev.auguste.agni_api.core.usecases.finance_principles.dto

import dev.auguste.agni_api.core.entities.enums.PrincipleType
import java.util.UUID

data class UpdateFinancePrincipleInput(
    val id: UUID,
    val name: String?,
    val description: String?,
    val targetType: PrincipleType?,
    val strictness: Int?,
    val logicRules: String?
)
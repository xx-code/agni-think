package dev.auguste.agni_api.core.usecases.finance_principles.dto

import dev.auguste.agni_api.core.entities.enums.PrincipleType

data class CreateFinancePrincipleInput(
    val name: String,
    val description: String,
    val targetType: PrincipleType,
    val strictness: Int,
    val logicRules: String?
)

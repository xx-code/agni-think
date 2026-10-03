package usecases.finance_principles.dto

import domain.enums.PrincipleType

data class CreateFinancePrincipleInput(
    val name: String,
    val description: String,
    val targetType: PrincipleType,
    val strictness: Int,
    val logicRules: String?
)

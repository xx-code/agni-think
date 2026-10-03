package usecases.finance_principles.dto

import domain.enums.PrincipleType
import java.util.UUID

data class UpdateFinancePrincipleInput(
    val id: UUID,
    val name: String?,
    val description: String?,
    val targetType: PrincipleType?,
    val strictness: Int?,
    val logicRules: String?
)
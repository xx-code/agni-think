package usecases.patrimonies.dto

import domain.enums.PatrimonyType
import java.util.UUID

data class CreatePatrimonyInput(
    val title: String,
    val amount: Double,
    val accountIds: Set<UUID>,
    val type: domain.enums.PatrimonyType
)

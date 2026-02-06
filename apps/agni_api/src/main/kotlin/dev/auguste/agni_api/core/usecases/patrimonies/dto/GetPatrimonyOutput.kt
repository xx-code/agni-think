package dev.auguste.agni_api.core.usecases.patrimonies.dto

import dev.auguste.agni_api.core.entities.enums.PatrimonyType
import java.util.UUID

data class GetPatrimonyOutput(
    val id: UUID,
    val title: String,
    val accountIds: List<UUID>,
    val amount: Double,
    val currentBalance: Double,
    val pastBalance: Double,
    val type: PatrimonyType
)

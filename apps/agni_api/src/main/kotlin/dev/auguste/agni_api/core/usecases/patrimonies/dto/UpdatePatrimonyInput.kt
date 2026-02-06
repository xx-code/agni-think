package dev.auguste.agni_api.core.usecases.patrimonies.dto

import dev.auguste.agni_api.core.entities.enums.PatrimonyType
import java.util.UUID

data class UpdatePatrimonyInput(
    val id: UUID,
    val title: String?,
    val amount: Double?,
    val accountIds: Set<UUID>?,
    val type: PatrimonyType?
)

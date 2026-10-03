package usecases.patrimonies.dto

import domain.enums.PatrimonyType
import java.util.UUID

data class UpdatePatrimonyInput(
    val id: UUID,
    val title: String?,
    val amount: Double?,
    val accountIds: Set<UUID>?,
    val type: PatrimonyType?
)

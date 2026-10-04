package usecases.patrimonies.dto

import java.util.UUID

data class GetPatrimonyInput(
    val id: UUID,
    val sourceType: SourcePatrimonyType = SourcePatrimonyType.PATRIMONY,
    val isAsset: Boolean = false
)
package usecases.patrimonies.snapshots.dto

import adapters.dto.QueryFilter
import usecases.patrimonies.dto.SourcePatrimonyType
import java.util.UUID

data class GetAllSnapshotPatrimonyInput(
    val patrimonyId: UUID,
    val query: QueryFilter,
    val sourcePatrimonyType: SourcePatrimonyType = SourcePatrimonyType.PATRIMONY,
    val isAsset: Boolean = false
)

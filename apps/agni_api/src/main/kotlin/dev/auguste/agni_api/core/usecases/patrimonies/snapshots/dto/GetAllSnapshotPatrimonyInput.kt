package dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import java.util.UUID

data class GetAllSnapshotPatrimonyInput(
    val patrimonyId: UUID,
    val query: QueryFilter
)

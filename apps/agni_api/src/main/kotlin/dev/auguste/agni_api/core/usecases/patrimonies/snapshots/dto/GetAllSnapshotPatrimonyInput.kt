package dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QuerySnapshotPatrimonyExtend
import java.util.UUID

data class GetAllSnapshotPatrimonyInput(
    val patrimonyId: UUID,
    val query: QueryFilter
)

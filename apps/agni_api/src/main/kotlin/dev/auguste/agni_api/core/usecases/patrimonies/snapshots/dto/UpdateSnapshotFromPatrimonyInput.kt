package dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto

import dev.auguste.agni_api.core.entities.enums.PatrimonySnapshotStatusType
import java.time.LocalDate
import java.util.Date
import java.util.UUID

data class UpdateSnapshotFromPatrimonyInput(
    val id: UUID,
    val balance: Double?,
    val status: PatrimonySnapshotStatusType?,
    val date: LocalDate?
)

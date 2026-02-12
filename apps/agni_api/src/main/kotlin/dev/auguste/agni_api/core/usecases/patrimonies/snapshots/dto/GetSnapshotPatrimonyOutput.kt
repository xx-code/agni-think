package dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto

import dev.auguste.agni_api.core.entities.enums.PatrimonySnapshotStatusType
import java.time.LocalDate
import java.util.Date
import java.util.UUID

data class GetSnapshotPatrimonyOutput(
    val id: UUID,
    val patrimonyId: UUID,
    val balance: Double,
    val date: LocalDate,
    val status: String
)
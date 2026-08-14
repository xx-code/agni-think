package dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto

import java.time.LocalDate
import java.util.UUID

data class GetSnapshotPatrimonyOutput(
    val id: UUID,
    val patrimonyId: UUID,
    val balance: Double,
    val date: LocalDate,
    val status: String
)
package usecases.patrimonies.snapshots.dto

import domain.enums.PatrimonySnapshotStatusType
import java.time.LocalDate
import java.util.UUID

data class AddSnapshotToPatrimonyInput(
    val patrimonyId: UUID,
    val balance: Double,
    val status: domain.enums.PatrimonySnapshotStatusType,
    val date: LocalDate
)

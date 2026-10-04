package usecases.patrimonies.snapshots.dto

import domain.enums.PatrimonySnapshotStatusType
import java.time.LocalDate
import java.util.UUID

data class UpdateSnapshotFromPatrimonyInput(
    val id: UUID,
    val balance: Double?,
    val status: PatrimonySnapshotStatusType?,
    val date: LocalDate?
)

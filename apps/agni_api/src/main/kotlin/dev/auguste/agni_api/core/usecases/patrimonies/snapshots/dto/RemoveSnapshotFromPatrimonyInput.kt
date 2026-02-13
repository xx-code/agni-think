package dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto

import java.util.UUID

data class RemoveSnapshotFromPatrimonyInput (
    val snapshotId: UUID
)
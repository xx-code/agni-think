package dev.auguste.agni_api.core.usecases.patrimonies.snapshots

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto.RemoveSnapshotFromPatrimonyInput
import java.util.UUID

class RemoveSnapshotFromPatrimony(private val snapshotRepo: IRepository<PatrimonySnapshot>): IUseCase<RemoveSnapshotFromPatrimonyInput, Unit> {

    override fun execAsync(input: RemoveSnapshotFromPatrimonyInput) {
        snapshotRepo.get(input.snapshotId) ?: throw Error("Snapshot not found")

        snapshotRepo.delete(input.snapshotId)
    }
}
package dev.auguste.agni_api.core.usecases.patrimonies.snapshots

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class RemoveSnapshotFromPatrimony(val snapshotRepo: IRepository<PatrimonySnapshot>): IUseCase<UUID, Unit> {

    override fun execAsync(input: UUID) {
        snapshotRepo.get(input) ?: throw Error("Snapshot not found")

        snapshotRepo.delete(input)
    }
}
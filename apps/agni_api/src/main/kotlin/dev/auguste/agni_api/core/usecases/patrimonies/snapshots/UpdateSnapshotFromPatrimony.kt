package dev.auguste.agni_api.core.usecases.patrimonies.snapshots

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto.UpdateSnapshotFromPatrimonyInput

class UpdateSnapshotFromPatrimony(
    private val snapshotRepo: IRepository<PatrimonySnapshot>
): IUseCase<UpdateSnapshotFromPatrimonyInput, Unit> {

    override fun execAsync(input: UpdateSnapshotFromPatrimonyInput) {
        val snapshot = snapshotRepo.get(input.id) ?: throw Error("No snapshot  found")

        if (input.balance != null)
            snapshot.currentBalanceObserved = input.balance

        if (input.status != null)
            snapshot.status = input.status

        if (input.date != null)
            snapshot.date = input.date

        if (snapshot.hasChanged())
            snapshotRepo.update(snapshot)
    }
}
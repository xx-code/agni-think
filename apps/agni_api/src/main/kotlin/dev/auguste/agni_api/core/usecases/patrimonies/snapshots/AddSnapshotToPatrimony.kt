package dev.auguste.agni_api.core.usecases.patrimonies.snapshots

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Patrimony
import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto.AddSnapshotToPatrimonyInput

class AddSnapshotToPatrimony(
    val patrimonyRepo: IRepository<Patrimony>,
    val snapshotPatrimonyRepo: IRepository<PatrimonySnapshot>
): IUseCase<AddSnapshotToPatrimonyInput, CreatedOutput> {

    override fun execAsync(input: AddSnapshotToPatrimonyInput): CreatedOutput {
        patrimonyRepo.get(input.patrimonyId) ?: throw Error("Patrimony not found")

        val snapShot = PatrimonySnapshot(
            patrimonyId = input.patrimonyId,
            currentBalanceObserved = input.balance,
            date = input.date,
            status = input.status
        )

        snapshotPatrimonyRepo.create(snapShot)

        return CreatedOutput(snapShot.id)
    }
}
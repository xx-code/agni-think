package usecases.patrimonies.snapshots

import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.Patrimony
import domain.entities.PatrimonySnapshot
import domain.entities.Provision
import usecases.CreatedOutput
import usecases.interfaces.IUseCase
import usecases.patrimonies.snapshots.dto.AddSnapshotToPatrimonyInput

class AddSnapshotToPatrimony(
    private val patrimonyRepo: IRepository<Patrimony>,
    private val snapshotPatrimonyRepo: IRepository<PatrimonySnapshot>
): IUseCase<AddSnapshotToPatrimonyInput, CreatedOutput> {

    override fun execAsync(input: AddSnapshotToPatrimonyInput): CreatedOutput {
        patrimonyRepo.get(input.patrimonyId) ?: throw NotFoundException.SingleEntity(input.patrimonyId, "patrimony")

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
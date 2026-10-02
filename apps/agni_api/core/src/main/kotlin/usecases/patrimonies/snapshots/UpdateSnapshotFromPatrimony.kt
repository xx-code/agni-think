package usecases.patrimonies.snapshots

import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.PatrimonySnapshot
import usecases.interfaces.IUseCase
import usecases.patrimonies.snapshots.dto.UpdateSnapshotFromPatrimonyInput

class UpdateSnapshotFromPatrimony(
    private val snapshotRepo: IRepository<PatrimonySnapshot>
): IUseCase<UpdateSnapshotFromPatrimonyInput, Unit> {

    override fun execAsync(input: UpdateSnapshotFromPatrimonyInput) {
        val snapshot = snapshotRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "snapshot")

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
package usecases.patrimonies.snapshots

import adapters.repositories.IRepository
import domain.entities.PatrimonySnapshot
import usecases.interfaces.IUseCase
import domain.exceptions.NotFoundException
import usecases.patrimonies.snapshots.dto.RemoveSnapshotFromPatrimonyInput

class RemoveSnapshotFromPatrimony(private val snapshotRepo: IRepository<PatrimonySnapshot>): IUseCase<RemoveSnapshotFromPatrimonyInput, Unit> {

    override fun execAsync(input: RemoveSnapshotFromPatrimonyInput) {
        snapshotRepo.get(input.snapshotId) ?: throw NotFoundException.SingleEntity(input.snapshotId, "snapshot")

        snapshotRepo.delete(input.snapshotId)
    }
}
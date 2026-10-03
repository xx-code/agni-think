package usecases.patrimonies.snapshots

import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.PatrimonySnapshot
import domain.exceptions.NotFoundException
import usecases.patrimonies.snapshots.dto.RemoveSnapshotFromPatrimonyInput

class RemoveSnapshotFromPatrimony(private val snapshotRepo: IRepository<PatrimonySnapshot>): UseCase<RemoveSnapshotFromPatrimonyInput, Unit>() {

    override suspend fun process(input: RemoveSnapshotFromPatrimonyInput) {
        snapshotRepo.get(input.snapshotId) ?: throw NotFoundException.SingleEntity(input.snapshotId, "snapshot")

        snapshotRepo.delete(input.snapshotId)
    }
}
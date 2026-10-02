package usecases.patrimonies

import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.dto.QueryFilter
import adapters.repositories.query_extend.QueryPatrimonySnapshotExtend
import domain.entities.Patrimony
import domain.entities.PatrimonySnapshot
import usecases.interfaces.IUseCase
import domain.exceptions.NotFoundException
import usecases.patrimonies.dto.DeletePatrimonyInput

class DeletePatrimony(
    private val patrimonyRepo: IRepository<Patrimony>,
    private val patrimonySnapshotRepo: IRepository<PatrimonySnapshot>,
    private val unitOfWork: IUnitOfWork): IUseCase<DeletePatrimonyInput, Unit> {

    override fun execAsync(input: DeletePatrimonyInput) {
        unitOfWork.execute {
            patrimonyRepo.get(input.patrimonyId) ?: throw NotFoundException.SingleEntity(input.patrimonyId, "patrimony")

            patrimonySnapshotRepo.getAll(query = QueryFilter(0, 0, true), QueryPatrimonySnapshotExtend(setOf(input.patrimonyId)))

            patrimonyRepo.delete(input.patrimonyId)
        }
    }
}
package usecases.patrimonies

import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.dto.QueryFilter
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
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

            val conditionSnapShot = QueryExtendBuilder<PatrimonySnapshot>()
                .addCondition("patrimonyId", QueryComparator.Equal, input.patrimonyId)
            patrimonySnapshotRepo.getAll(query = QueryFilter(0, 0, true), conditionSnapShot)

            patrimonyRepo.delete(input.patrimonyId)
        }
    }
}
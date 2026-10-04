package usecases.patrimonies

import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.dto.QueryFilter
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Patrimony
import domain.entities.PatrimonySnapshot
import domain.exceptions.NotFoundException
import usecases.UseCase
import usecases.patrimonies.dto.DeletePatrimonyInput

class DeletePatrimony(
    private val patrimonyRepo: IRepository<Patrimony>,
    private val patrimonySnapshotRepo: IRepository<PatrimonySnapshot>,
    unitOfWork: IUnitOfWork): UseCase<DeletePatrimonyInput, Unit>(unitOfWork) {

    override suspend fun process(input: DeletePatrimonyInput) {
        patrimonyRepo.get(input.patrimonyId) ?: throw NotFoundException.SingleEntity(input.patrimonyId, "patrimony")

        val conditionSnapShot = QueryExtendBuilder<PatrimonySnapshot>()
            .addCondition("patrimonyId", QueryComparator.Equal, input.patrimonyId)
        patrimonySnapshotRepo.getAll(query = QueryFilter(0, 0, true), conditionSnapShot)

        patrimonyRepo.delete(input.patrimonyId)
    }
}
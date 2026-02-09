package dev.auguste.agni_api.core.usecases.patrimonies

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryPatrimonySnapshotExtend
import dev.auguste.agni_api.core.entities.Patrimony
import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.patrimonies.dto.DeletePatrimonyInput
import java.util.UUID

class DeletePatrimony(
    private val patrimonyRepo: IRepository<Patrimony>,
    private val patrimonySnapshotRepo: IRepository<PatrimonySnapshot>,
    private val unitOfWork: IUnitOfWork): IUseCase<DeletePatrimonyInput, Unit> {

    override fun execAsync(input: DeletePatrimonyInput) {
        unitOfWork.execute {
            patrimonyRepo.get(input.patrimonyId) ?: throw Error("Patrimony not found")

            patrimonySnapshotRepo.getAll(query = QueryFilter(0, 0, true), QueryPatrimonySnapshotExtend(setOf(input.patrimonyId)))

            patrimonyRepo.delete(input.patrimonyId)
        }
    }
}
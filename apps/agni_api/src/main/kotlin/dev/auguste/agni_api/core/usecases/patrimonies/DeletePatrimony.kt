package dev.auguste.agni_api.core.usecases.patrimonies

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QuerySnapshotPatrimonyExtend
import dev.auguste.agni_api.core.entities.Patrimony
import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class DeletePatrimony(
    val patrimonyRepo: IRepository<Patrimony>,
    val patrimonySnapshotRepo: IRepository<PatrimonySnapshot>,
    val unitOfWork: IUnitOfWork): IUseCase<UUID, Unit> {

    override fun execAsync(input: UUID) {
        try {
            unitOfWork.start()

            patrimonyRepo.get(input) ?: throw Error("Patrimony not found")

            patrimonySnapshotRepo.getAll(query = QueryFilter(0, 0, true), QuerySnapshotPatrimonyExtend(setOf(input)))

            patrimonyRepo.delete(input)

            unitOfWork.commit()
        } catch (error: Throwable) {
            unitOfWork.rollback()
            throw error
        }
    }
}
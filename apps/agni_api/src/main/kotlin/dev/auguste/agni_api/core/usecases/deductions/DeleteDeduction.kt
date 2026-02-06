package dev.auguste.agni_api.core.usecases.deductions

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Deduction
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class DeleteDeduction(val deductionRepo: IRepository<Deduction>): IUseCase<UUID, Unit> {

    override fun execAsync(input: UUID) {
        deductionRepo.get(input) ?: throw Error("deduction ${input} not found")

        deductionRepo.delete(input)
    }
}
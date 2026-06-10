package dev.auguste.agni_api.core.usecases.deductions

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Deduction
import dev.auguste.agni_api.core.usecases.deductions.dto.DeleteDeductionInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.entities.DomainException
import java.util.UUID

class DeleteDeduction(private val deductionRepo: IRepository<Deduction>): IUseCase<DeleteDeductionInput, Unit> {

    override fun execAsync(input: DeleteDeductionInput) {
        deductionRepo.get(input.deductionId) ?: throw DomainException.NotFound.Deduction(input.deductionId.toString())

        deductionRepo.delete(input.deductionId)
    }
}
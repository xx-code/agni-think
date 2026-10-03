package usecases.deductions

import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.Deduction
import usecases.deductions.dto.DeleteDeductionInput
import domain.exceptions.NotFoundException

class DeleteDeduction(private val deductionRepo: IRepository<Deduction>): UseCase<DeleteDeductionInput, Unit>() {

    override suspend fun process(input: DeleteDeductionInput) {
        deductionRepo.get(input.deductionId) ?: throw NotFoundException.SingleEntity(input.deductionId, "deduction")

        deductionRepo.delete(input.deductionId)
    }
}
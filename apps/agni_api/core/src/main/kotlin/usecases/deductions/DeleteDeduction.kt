package usecases.deductions

import adapters.repositories.IRepository
import domain.entities.Deduction
import usecases.deductions.dto.DeleteDeductionInput
import usecases.interfaces.IUseCase
import domain.exceptions.NotFoundException

class DeleteDeduction(private val deductionRepo: IRepository<Deduction>): IUseCase<DeleteDeductionInput, Unit> {

    override fun execAsync(input: DeleteDeductionInput) {
        deductionRepo.get(input.deductionId) ?: throw NotFoundException.SingleEntity(input.deductionId, "deduction")

        deductionRepo.delete(input.deductionId)
    }
}
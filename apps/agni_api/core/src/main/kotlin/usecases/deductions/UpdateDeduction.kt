package usecases.deductions

import adapters.repositories.IRepository
import domain.entities.Deduction
import usecases.deductions.dto.UpdateDeductionInput
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import usecases.interfaces.IUseCase

class UpdateDeduction(private val deductionRepo: IRepository<Deduction>): IUseCase<UpdateDeductionInput, Unit> {

    override fun execAsync(input: UpdateDeductionInput) {
        val deduction = deductionRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "deduction")

        if (input.title != null) {
            if (input.title != deduction.title && deductionRepo.existsByName(input.title))
                throw AlreadyExistException.EntitiesByField(mapOf("name" to input.title), "deduction")

            deduction.title = input.title
        }

        if (input.description != null)
            deduction.description = input.description

        if (deduction.hasChanged())
            deductionRepo.update(deduction)
    }
}
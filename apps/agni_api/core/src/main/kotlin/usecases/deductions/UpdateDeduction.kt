package usecases.deductions

import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.Deduction
import usecases.deductions.dto.UpdateDeductionInput
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
class UpdateDeduction(private val deductionRepo: IRepository<Deduction>): UseCase<UpdateDeductionInput, Unit>() {

    override suspend fun process(input: UpdateDeductionInput) {
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
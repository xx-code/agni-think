package usecases.deductions

import adapters.repositories.IRepository
import domain.entities.Deduction
import usecases.CreatedOutput
import usecases.deductions.dto.CreateDeductionInput
import domain.exceptions.AlreadyExistException
import usecases.interfaces.IUseCase

class CreateDeduction(private val deductionRepo: IRepository<Deduction>): IUseCase<CreateDeductionInput, CreatedOutput> {

    override fun execAsync(input: CreateDeductionInput): CreatedOutput {
        if (deductionRepo.existsByName(input.title))
            throw AlreadyExistException.EntitiesByField(mapOf("name" to input.title), "deduction")

        val newDeduction = Deduction(
            title = input.title,
            description = input.description,
            base = input.base,
            mode = input.mode
        )

        deductionRepo.create(newDeduction)

        return CreatedOutput(newDeduction.id)
    }
}
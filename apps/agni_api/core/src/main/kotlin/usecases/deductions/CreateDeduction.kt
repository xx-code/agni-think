package usecases.deductions

import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.Deduction
import usecases.dto.CreatedOutput
import usecases.deductions.dto.CreateDeductionInput
import domain.exceptions.AlreadyExistException
class CreateDeduction(private val deductionRepo: IRepository<Deduction>): UseCase<CreateDeductionInput, CreatedOutput>() {

    override suspend fun process(input: CreateDeductionInput): CreatedOutput {
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
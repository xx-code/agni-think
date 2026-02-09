package dev.auguste.agni_api.core.usecases.deductions

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Deduction
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.deductions.dto.CreateDeductionInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class CreateDeduction(private val deductionRepo: IRepository<Deduction>): IUseCase<CreateDeductionInput, CreatedOutput> {

    override fun execAsync(input: CreateDeductionInput): CreatedOutput {
        if (deductionRepo.existsByName(input.title))
            throw Error("Deduction name already exists")

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
package dev.auguste.agni_api.core.usecases.deductions

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Deduction
import dev.auguste.agni_api.core.usecases.deductions.dto.UpdateDeductionInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class UpdateDeduction(val deductionRepo: IRepository<Deduction>): IUseCase<UpdateDeductionInput, Unit> {

    override fun execAsync(input: UpdateDeductionInput) {
        val deduction = deductionRepo.get(input.id) ?: throw Error("deduction ${input.id} not found")

        if (input.title != null) {
            if (input.title != deduction.title && deductionRepo.existsByName(input.title))
                throw Error("deduction title ${input.title} already exists")

            deduction.title = input.title
        }

        if (input.description != null)
            deduction.description = input.description

        if (deduction.hasChanged())
            deductionRepo.update(deduction)
    }
}
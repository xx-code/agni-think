package dev.auguste.agni_api.core.usecases.deductions

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Deduction
import dev.auguste.agni_api.core.usecases.deductions.dto.UpdateDeductionInput
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class UpdateDeduction(private val deductionRepo: IRepository<Deduction>): IUseCase<UpdateDeductionInput, Unit> {

    override fun execAsync(input: UpdateDeductionInput) {
        val deduction = deductionRepo.get(input.id) ?: throw DomainException.NotFound.Deduction(input.id)

        if (input.title != null) {
            if (input.title != deduction.title && deductionRepo.existsByName(input.title))
                throw DomainException.AlreadyExist.Deduction(input.title)

            deduction.title = input.title
        }

        if (input.description != null)
            deduction.description = input.description

        if (deduction.hasChanged())
            deductionRepo.update(deduction)
    }
}
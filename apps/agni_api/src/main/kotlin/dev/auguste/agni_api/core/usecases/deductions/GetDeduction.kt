package dev.auguste.agni_api.core.usecases.deductions

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Deduction
import dev.auguste.agni_api.core.usecases.deductions.dto.GetDeductionOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class GetDeduction(private val deductionRepo: IRepository<Deduction>): IUseCase<UUID, GetDeductionOutput> {
    override fun execAsync(input: UUID): GetDeductionOutput {
        val deduction = deductionRepo.get(input) ?: throw Error("deduction id $input not found")

        return GetDeductionOutput(
            id = deduction.id,
            title = deduction.title,
            description = deduction.description,
            base = deduction.base.value,
            mode = deduction.mode.value
        )
    }
}
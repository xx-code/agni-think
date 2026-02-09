package dev.auguste.agni_api.core.usecases.deductions

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.entities.Deduction
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.deductions.dto.GetDeductionOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class GetAllDeductions(private val deductionRepo: IRepository<Deduction>): IUseCase<QueryFilter, ListOutput<GetDeductionOutput>> {

    override fun execAsync(input: QueryFilter): ListOutput<GetDeductionOutput> {
        val deductions = deductionRepo.getAll(input)

        return ListOutput(
            items = deductions.items.map {
                GetDeductionOutput(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    base = it.base,
                    mode = it.mode
                )
            },
            total = deductions.total
        )
    }
}
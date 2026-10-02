package usecases.deductions

import adapters.repositories.IRepository
import adapters.dto.QueryFilter
import domain.entities.Deduction
import usecases.ListOutput
import usecases.deductions.dto.GetDeductionOutput
import usecases.interfaces.IUseCase

class GetAllDeductions(private val deductionRepo: IRepository<Deduction>): IUseCase<QueryFilter, ListOutput<GetDeductionOutput>> {

    override fun execAsync(input: QueryFilter): ListOutput<GetDeductionOutput> {
        val deductions = deductionRepo.getAll(input)

        return ListOutput(
            items = deductions.items.map {
                GetDeductionOutput(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    base = it.base.value,
                    mode = it.mode.value
                )
            },
            total = deductions.total
        )
    }
}
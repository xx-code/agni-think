package usecases.deductions

import usecases.UseCase
import adapters.repositories.IRepository
import adapters.dto.QueryFilter
import domain.entities.Deduction
import usecases.dto.ListOutput
import usecases.deductions.dto.GetDeductionOutput
class GetAllDeductions(private val deductionRepo: IRepository<Deduction>): UseCase<QueryFilter, ListOutput<GetDeductionOutput>>() {

    override suspend fun process(input: QueryFilter): ListOutput<GetDeductionOutput> {
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
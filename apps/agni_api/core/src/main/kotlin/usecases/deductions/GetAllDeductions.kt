package usecases.deductions

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.repositories.IRepository
import adapters.dto.QueryFilter
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.entities.Deduction
import usecases.deductions.dto.GetAllDeductionInput
import usecases.dto.ListOutput
import usecases.deductions.dto.GetDeductionOutput
class GetAllDeductions(private val deductionRepo: IRepository<Deduction>, auth: IAuthenticateAdapter): UseCase<GetAllDeductionInput, ListOutput<GetDeductionOutput>>(auth = auth) {

    override suspend fun process(input: GetAllDeductionInput): ListOutput<GetDeductionOutput> {
        val currentUser = getCurrentUser()
        val condition = QueryExtendBuilder<Deduction>()
            .addCondition("userId", QueryComparator.Equal, currentUser.id)
        val deductions = deductionRepo.getAll(input.queryFilter, condition)

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
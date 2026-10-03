package usecases.funds

import usecases.UseCase
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Fund
import domain.exceptions.NotFoundException
import domain.entities.Goal
import usecases.funds.dto.FundGoalOutput
import usecases.funds.dto.GetSavingGoalOutput
import java.util.UUID

class GetFund(
    private val fundRepo: IRepository<Fund>,
    private val goalRepo: IRepository<Goal>
    ): UseCase<UUID, GetSavingGoalOutput>() {

    override suspend fun process(input: UUID): GetSavingGoalOutput {
        val fund = fundRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "saving_goal")
        val conditionGoal = QueryExtendBuilder<Goal>()
            .addCondition("targetSourceId", QueryComparator.Equal, fund.id)
        val goals = goalRepo.getAll(QueryFilter.queryAll(), conditionGoal)

        return GetSavingGoalOutput(
            id = fund.id,
            title = fund.title,
            description = fund.description,
            target = fund.target,
            balance = fund.balance,
            accountId = fund.accountId,
            type = fund.type.value,
            goals = goals.items.map {
                FundGoalOutput(
                    id = it.id,
                    title = it.title,
                    dueDate = it.dueDate
                )
            }
        )
    }
}
package dev.auguste.rest_api.controllers.models

import domain.enums.FundType
import usecases.funds.dto.CreateFundInput
import usecases.funds.dto.UpdateFundInput
import java.util.UUID


data class ApiUpgradeSavingGoalModel(
    val accountId: UUID,
    val amount: Double,
)

data class ApiCreateSavingGoalModel(
    val title: String,
    val target: Double,
    val type: String,
    val description: String,
    val accountId: UUID?
)

data class ApiUpdateSavingGoalModel(
    val title: String?,
    val target: Double?,
    val type: String?,
    val description: String?,
    val accountId: UUID?,
)

data class ApiDeleteSavingGoalModel(
    val accountId: UUID?,
)


fun mapApiCreateSavingGoal(model: ApiCreateSavingGoalModel): CreateFundInput {
    return CreateFundInput(
        target = model.target,
        title = model.title,
        description = model.description,
        type = FundType.fromString(model.type),
        accountId = model.accountId
    )
}

fun mapApiUpdateSavingGoal(id: UUID, model: ApiUpdateSavingGoalModel): UpdateFundInput {
    return UpdateFundInput(
        id = id,
        target = model.target,
        title = model.title,
        description = model.description,
        type = model.type?.let { FundType.fromString(model.type) },
        accountId = model.accountId
    )
}
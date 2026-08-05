package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.usecases.saving_goals.dto.CreateSavingGoalInput
import dev.auguste.agni_api.core.usecases.saving_goals.dto.UpdateSavingGoalInput
import java.time.LocalDate
import java.util.UUID


data class ApiUpgradeSavingGoalModel(
    val accountId: UUID?,
    val amount: Double,
)

data class ApiCreateSavingGoalModel(
    val title: String,
    val target: Double,
    val description: String,
    val accountId: UUID?
)

data class ApiUpdateSavingGoalModel(
    val title: String?,
    val target: Double?,
    val description: String?,
    val accountId: UUID?,
)

data class ApiDeleteSavingGoalModel(
    val accountId: UUID?,
)


fun mapApiCreateSavingGoal(model: ApiCreateSavingGoalModel): CreateSavingGoalInput {
    return CreateSavingGoalInput(
        target = model.target,
        title = model.title,
        description = model.description,
        accountId = model.accountId
    )
}

fun mapApiUpdateSavingGoal(id: UUID, model: ApiUpdateSavingGoalModel): UpdateSavingGoalInput {
    return UpdateSavingGoalInput(
        id = id,
        target = model.target,
        title = model.title,
        description = model.description,
        accountId = model.accountId
    )
}
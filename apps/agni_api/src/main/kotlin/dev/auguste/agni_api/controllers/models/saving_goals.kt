package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.entities.enums.ImportanceGoalType
import dev.auguste.agni_api.core.entities.enums.IntensityEmotionalDesirType
import dev.auguste.agni_api.core.usecases.saving_goals.dto.CreateSavingGoalInput
import dev.auguste.agni_api.core.usecases.saving_goals.dto.ItemSavingGoalInput
import dev.auguste.agni_api.core.usecases.saving_goals.dto.UpdateSavingGoalInput
import java.time.LocalDate
import java.util.UUID

data class ApiItemSavingGoalModel(
    val title: String,
    val price: Double,
    val url: String
)

data class ApiUpgradeSavingGoalModel(
    val accountId: UUID?,
    val amount: Double,
)

data class ApiCreateSavingGoalModel(
    val title: String,
    val target: Double,
    val description: String,
    val accountId: UUID?,
    val desirValue: IntensityEmotionalDesirType,
    val importance: ImportanceGoalType,
    val wishDueDate: LocalDate?,
    val items: Set<ApiItemSavingGoalModel>
)

data class ApiUpdateSavingGoalModel(
    val title: String?,
    val target: Double?,
    val description: String?,
    val accountId: UUID?,
    val desirValue: IntensityEmotionalDesirType?,
    val importance: ImportanceGoalType?,
    val wishDueDate: LocalDate?,
    val items: Set<ApiItemSavingGoalModel>?
)

data class ApiDeleteSavingGoalModel(
    val accountId: UUID?,
)


fun mapApiCreateSavingGoal(model: ApiCreateSavingGoalModel): CreateSavingGoalInput {
    return CreateSavingGoalInput(
        target = model.target,
        title = model.title,
        description = model.description,
        accountId = model.accountId,
        desirValue = model.desirValue,
        importance = model.importance,
        wishDueDate = model.wishDueDate,
        items = model.items.map {
            ItemSavingGoalInput(
                title = it.title,
                price = it.price,
                url = it.url
            )
        }.toSet()
    )
}

fun mapApiUpdateSavingGoal(id: UUID, model: ApiUpdateSavingGoalModel): UpdateSavingGoalInput {
    return UpdateSavingGoalInput(
        id = id,
        target = model.target,
        title = model.title,
        description = model.description,
        accountId = model.accountId,
        desirValue = model.desirValue,
        importance = model.importance,
        wishDueDate = model.wishDueDate,
        items = model.items?.map {
            ItemSavingGoalInput(
                title = it.title,
                price = it.price,
                url = it.url
            )
        }?.toSet() ?: setOf()
    )
}
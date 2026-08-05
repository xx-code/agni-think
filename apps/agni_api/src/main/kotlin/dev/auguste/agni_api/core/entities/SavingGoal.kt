package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.entities.enums.ImportanceGoalType
import dev.auguste.agni_api.core.entities.enums.IntensityEmotionalDesirType
import dev.auguste.agni_api.core.value_objects.SavingGoalItem
import java.time.LocalDate
import java.util.Date
import java.util.UUID
import kotlin.properties.Delegates

class SavingGoal(
    id: UUID = UUID.randomUUID(),
    title: String,
    description: String,
    target: Double,
    balance: Double,
    accountId: UUID?
): Entity(id = id) {

    var accountId by cleanObservable(accountId, this)

    var title by cleanObservable(title, this)

    var description by cleanObservable(description, this)

    var target by cleanObservable(target, this)

    var balance by cleanObservable(balance, this)
}
package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.entities.enums.FundType
import java.util.UUID

class SavingGoal(
    id: UUID = UUID.randomUUID(),
    title: String,
    description: String,
    target: Double,
    balance: Double,
    type: FundType,
    accountId: UUID?
): Entity(id = id) {

    var accountId by cleanObservable(accountId, this)

    var title by cleanObservable(title, this)

    var description by cleanObservable(description, this)

    var target by cleanObservable(target, this)

    var balance by cleanObservable(balance, this)

    var type by cleanObservable(type, this)
}
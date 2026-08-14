package dev.auguste.agni_api.core.entities

import java.util.UUID

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
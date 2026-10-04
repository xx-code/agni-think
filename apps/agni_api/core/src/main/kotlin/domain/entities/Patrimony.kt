package domain.entities

import domain.enums.PatrimonyType
import java.util.UUID


class Patrimony(
    id: UUID = UUID.randomUUID(),
    title: String,
    amount: Double,
    accountIds: MutableSet<UUID> = mutableSetOf(),
    type: PatrimonyType
    ): Entity(id = id) {

    var title: String by cleanObservable(title, this)

    var amount: Double by cleanObservable(amount, this)

    var type by cleanObservable(type, this)

    var accountIds: MutableSet<UUID> by cleanObservable(accountIds, this)
}
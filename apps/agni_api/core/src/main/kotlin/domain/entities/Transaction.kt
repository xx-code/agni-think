package domain.entities

import domain.exceptions.ValidationException
import java.util.UUID

class Transaction(
    id: UUID = UUID.randomUUID(),
    val invoiceId: UUID,
    categoryId: UUID,
    amount: Double,
    tagIds: MutableSet<UUID> = mutableSetOf(),
    budgetIds: MutableSet<UUID> = mutableSetOf(),
    description: String): Entity(id = id) {

    var tagIds by cleanObservable(tagIds, this)

    var budgetIds by cleanObservable(budgetIds, this)

    var categoryId by cleanObservable(categoryId, this)

    var amount: Double by cleanObservable(amount, this, {
        it > 0.0
    }) {
        ValidationException.TransactionAmountMustBeGreaterThanZero(it)
    }

    var description: String by cleanObservable(description, this)
}
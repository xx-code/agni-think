package domain.entities

import domain.enums.FundType
import domain.exceptions.ValidationException
import java.util.UUID

class Fund(
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

    var target by cleanObservable(target, this, {
        it > 0
    }) {
        ValidationException.FundTargetAmountMustGreaterThanZero(it)
    }

    var balance by cleanObservable(balance, this)

    var type by cleanObservable(type, this)
}
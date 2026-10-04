package domain.entities

import domain.interfaces.IAccountDetail
import java.util.UUID


class Account(
    id: UUID = UUID.randomUUID(),
    title: String,
    balance: Double,
    detail: IAccountDetail,
    color: Color,
    currencyId: UUID?,
    ): Entity(id = id) {

    var title: String by cleanObservable(title, this)

    var balance: Double by cleanObservable(balance, this)

    var detail: IAccountDetail by cleanObservable(detail, this)

    var currencyId: UUID? by cleanObservable(currencyId, this)

    var color: Color by cleanObservable(color, this)
}
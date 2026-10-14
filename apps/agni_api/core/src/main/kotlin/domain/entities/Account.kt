package domain.entities

import domain.interfaces.IAccountDetail
import domain.value_objects.CheckingAccountDetail
import java.util.UUID


class Account(
    id: UUID = UUID.randomUUID(),
    val userId: UUID,
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

    companion object {
        fun mock(
            id: UUID = UUID.randomUUID(),
            userId: UUID = UUID.randomUUID(),
            title: String = "Account",
            balance: Double = 0.0,
            color: Color = Color("#10a096"),
            detail: IAccountDetail = CheckingAccountDetail(0.0),
            currencyId: UUID? = null,
        ): Account {
            return Account(
                id = id,
                userId = userId,
                title = title,
                balance = balance,
                color = color,
                detail = detail,
                currencyId = currencyId
            )
        }
    }
}
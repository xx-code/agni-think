package domain.entities

import java.util.UUID

class Currency(
    id: UUID = UUID.randomUUID(),
    name: String,
    symbol: String,
    locale: String? = null,
    rateToBase: Double? = null,
    isBase: Boolean = false
): Entity(id = id) {
    var name: String by cleanObservable(name, this)

    var symbol: String by cleanObservable(symbol, this)

    var locale by cleanObservable(locale, this)

    var rateToBase by cleanObservable(rateToBase, this)

    var isBase by cleanObservable(isBase, this)
}
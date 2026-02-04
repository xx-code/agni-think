package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.entities.interfaces.IAccountDetail
import java.util.UUID
import kotlin.properties.Delegates

class Account(
    id: UUID = UUID.randomUUID(),
    title: String,
    balance: Double,
    detail: IAccountDetail,
    currencyId: UUID
    ): Entity(id = id) {

    var title: String by Delegates.observable(title) {
        prop, old, new ->
        this.markHasChanged()
    }

    var balance: Double by Delegates.observable(balance, { _, old, new ->
        if (old != new)
            this.markHasChanged()
    })

    var detail: IAccountDetail by Delegates.observable(detail, { _, old, new ->
        if (old != new)
            this.markHasChanged()
    })

    var currencyId: UUID by Delegates.observable(currencyId, { _, old, new ->
        if (old != new)
            this.markHasChanged()
    })
}
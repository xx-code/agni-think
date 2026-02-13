package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.entities.enums.PatrimonyType
import java.util.UUID
import kotlin.properties.Delegates

class Patrimony(
    id: UUID = UUID.randomUUID(),
    title: String,
    amount: Double,
    accountIds: MutableSet<UUID> = mutableSetOf(),
    type: PatrimonyType
    ): Entity(id = id) {

    var title: String by Delegates.observable(title) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var amount: Double by Delegates.observable(amount) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var type by Delegates.observable(type) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var accountIds: MutableSet<UUID> by Delegates.observable(accountIds) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }
}
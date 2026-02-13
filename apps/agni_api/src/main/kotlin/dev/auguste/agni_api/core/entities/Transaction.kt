package dev.auguste.agni_api.core.entities

import java.util.UUID
import kotlin.properties.Delegates

class Transaction(
    id: UUID = UUID.randomUUID(),
    val invoiceId: UUID,
    categoryId: UUID,
    amount: Double,
    tagIds: MutableSet<UUID> = mutableSetOf(),
    budgetIds: MutableSet<UUID> = mutableSetOf(),
    description: String): Entity(id = id) {

    var tagIds by Delegates.observable(tagIds) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var budgetIds by Delegates.observable(budgetIds) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var categoryId by Delegates.observable(categoryId) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var amount: Double by Delegates.observable(amount) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var description: String by Delegates.observable(description) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }
}
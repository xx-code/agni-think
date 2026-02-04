package dev.auguste.agni_api.core.entities

import java.util.UUID
import kotlin.properties.Delegates

class Transaction(
    id: UUID,
    invoiceId: UUID,
    tagIds: MutableSet<UUID>,
    budgetIds: MutableSet<UUID>,
    categoryIds: MutableSet<UUID>,
    amount: Double,
    description: String): Entity(id = id) {

    val invoiceId = invoiceId

    var tagIds by Delegates.observable(tagIds) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var budgetIds by Delegates.observable(budgetIds) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var categoryIds by Delegates.observable(categoryIds) { prop, old, new ->
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
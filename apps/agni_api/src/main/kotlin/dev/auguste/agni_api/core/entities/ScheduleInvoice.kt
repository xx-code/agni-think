package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.value_objects.Scheduler
import java.util.UUID
import kotlin.properties.Delegates

class ScheduleInvoice(
    id: UUID = UUID.randomUUID(),
    title: String,
    accountId: UUID,
    type: InvoiceType,
    amount: Double,
    scheduler: Scheduler,
    categoryId: UUID,
    isPause: Boolean = false,
    isFreeze: Boolean = false,
    tagIds: MutableSet<UUID> = mutableSetOf(),
): Entity(id = id) {

    var title: String by Delegates.observable(title) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var accountId: UUID by Delegates.observable(accountId) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var type by Delegates.observable(type) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var amount: Double by Delegates.observable(amount) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var scheduler: Scheduler by Delegates.observable(scheduler) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var categoryId: UUID by Delegates.observable(categoryId) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var isPause: Boolean by Delegates.observable(isPause) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var isFreeze: Boolean by Delegates.observable(isFreeze) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var tagIds by Delegates.observable(tagIds) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }
}
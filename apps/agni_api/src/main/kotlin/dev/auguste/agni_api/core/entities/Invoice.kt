package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.value_objects.InvoiceDeduction
import java.util.Date
import java.util.UUID
import kotlin.properties.Delegates

class Invoice(
    id: UUID = UUID.randomUUID(),
    accountId: UUID,
    status: InvoiceStatusType,
    mouvementType: InvoiceMouvementType,
    amount: Double,
    type: InvoiceType,
    deductions: MutableSet<InvoiceDeduction>,
    date: Date = Date(),
    isFreeze: Boolean = false,
): Entity(id = id) {
    var accountId: UUID by Delegates.observable(accountId) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var statusType: InvoiceStatusType by Delegates.observable(status) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var date: Date by Delegates.observable(date) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var isFreeze: Boolean by Delegates.observable(isFreeze) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var amount: Double by Delegates.observable(amount)  { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var type: InvoiceType by Delegates.observable(type) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var mouvementType: InvoiceMouvementType by Delegates.observable(mouvementType) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var deductions by Delegates.observable(deductions) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }
}
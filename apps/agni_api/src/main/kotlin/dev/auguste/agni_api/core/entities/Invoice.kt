package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.value_objects.InvoiceDeduction
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID
import kotlin.properties.Delegates

class Invoice(
    id: UUID = UUID.randomUUID(),
    accountId: UUID,
    status: InvoiceStatusType,
    mouvementType: InvoiceMouvementType,
    type: InvoiceType,
    deductions: MutableSet<InvoiceDeduction> = mutableSetOf(),
    date: LocalDateTime = LocalDateTime.now(),
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

    var date by Delegates.observable(date) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var isFreeze: Boolean by Delegates.observable(isFreeze) { prop, old, new ->
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
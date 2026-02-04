package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.entities.enums.PatrimonySnapshotStatusType
import java.util.Date
import java.util.UUID
import kotlin.properties.Delegates

class PatrimonySnapshot(
    id: UUID = UUID.randomUUID(),
    date: Date = Date(),
    currentBalanceObserved: Double,
    status: PatrimonySnapshotStatusType
    ): Entity(id = id) {

    var date: Date by Delegates.observable(date) { _, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var currentBalanceObserved: Double by Delegates.observable(currentBalanceObserved) { _, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var status: PatrimonySnapshotStatusType by Delegates.observable(status) { _, old, new ->
        if (old != new)
            this.markHasChanged()
    }
}
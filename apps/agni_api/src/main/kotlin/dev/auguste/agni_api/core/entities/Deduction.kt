package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.entities.enums.DeductionBaseType
import dev.auguste.agni_api.core.entities.enums.DeductionModeType
import java.util.UUID
import kotlin.properties.Delegates

class Deduction(
    id: UUID = UUID.randomUUID(),
    title: String,
    base: DeductionBaseType,
    mode: DeductionModeType,
    description: String,
): Entity(id = id) {
    val base = base
    val mode = mode

    var title: String by Delegates.observable(title) { _, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var description: String by Delegates.observable(description) { _, old, new ->
        if (old != new)
            this.markHasChanged()
    }
}
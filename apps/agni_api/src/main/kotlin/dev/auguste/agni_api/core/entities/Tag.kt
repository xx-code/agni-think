package dev.auguste.agni_api.core.entities

import java.util.UUID
import kotlin.properties.Delegates

class Tag(
    id: UUID = UUID.randomUUID(),
    value: String,
    color: String,
    isSystem: Boolean = false
) : Entity(id = id) {

    var value: String  by Delegates.observable(value) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var color: String by Delegates.observable(color) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var isSystem: Boolean by Delegates.observable(isSystem) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }
}
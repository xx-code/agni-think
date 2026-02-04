package dev.auguste.agni_api.core.entities

import java.util.UUID
import kotlin.properties.Delegates

class Category(
    id: UUID = UUID.randomUUID(),
    title: String,
    icon: String,
    colo: String,
    isSystem: Boolean = false
): Entity(id = id) {
    var title: String by Delegates.observable(title) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var icon: String by Delegates.observable(icon) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var colo: String by Delegates.observable(colo) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var isSystem: Boolean by Delegates.observable(isSystem) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }
}
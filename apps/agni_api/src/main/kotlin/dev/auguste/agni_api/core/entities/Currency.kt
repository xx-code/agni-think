package dev.auguste.agni_api.core.entities

import java.util.Locale
import java.util.UUID
import kotlin.properties.Delegates

class Currency(
    id: UUID = UUID.randomUUID(),
    name: String,
    symbol: String,
    locale: String? = null,
    rateToBase: Double? = null,
    isBase: Boolean = false
): Entity(id = id) {
    var name: String by Delegates.observable(name) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var symbol: String by Delegates.observable(symbol) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var locale by Delegates.observable(locale) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var rateToBase by Delegates.observable(rateToBase) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var isBase by Delegates.observable(isBase) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }
}
package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.entities.enums.ImportanceGoalType
import dev.auguste.agni_api.core.entities.enums.IntensityEmotionalDesirType
import dev.auguste.agni_api.core.value_objects.SavingGoalItem
import java.util.Date
import java.util.UUID
import kotlin.properties.Delegates

class SavingGoal(
    id: UUID = UUID.randomUUID(),
    title: String,
    description: String,
    target: Double,
    balance: Double,
    desired: IntensityEmotionalDesirType,
    importance: ImportanceGoalType,
    wishDueDate: Date,
    itemsToTracks: List<SavingGoalItem>,
    accountId: UUID?
): Entity(id = id) {

    var title: String by Delegates.observable(title) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var description: String by Delegates.observable(description) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var target: Double by Delegates.observable(target) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var balance: Double by Delegates.observable(balance) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var desired: IntensityEmotionalDesirType by Delegates.observable(desired) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var importance: ImportanceGoalType by Delegates.observable(importance) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var wishDueDate: Date by Delegates.observable(wishDueDate) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }
}
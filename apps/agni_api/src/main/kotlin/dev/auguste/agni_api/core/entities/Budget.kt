package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.value_objects.Scheduler
import java.util.UUID
import kotlin.properties.Delegates

class Budget(
    id: UUID = UUID.randomUUID(),
    title: String,
    target: Double,
    scheduler: Scheduler,
    targetSavingGoalIds: MutableSet<UUID>,
    isArchived: Boolean = false,
    ): Entity(id=id) {

    var title: String by Delegates.observable(title) { _, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var target: Double by Delegates.observable(target) { _, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var scheduler: Scheduler by Delegates.observable(scheduler) { _, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var targetSavingGoalIds by Delegates.observable(targetSavingGoalIds) { _, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var isArchived: Boolean by Delegates.observable(isArchived) { _, old, new ->
        if (old != new)
            this.markHasChanged()
    }
}
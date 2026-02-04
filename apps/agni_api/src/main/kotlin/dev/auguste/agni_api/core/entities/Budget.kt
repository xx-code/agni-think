package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.value_objects.BudgetSavingGoal
import dev.auguste.agni_api.core.value_objects.Scheduler
import java.util.UUID
import kotlin.properties.Delegates

class Budget(
    id: UUID = UUID.randomUUID(),
    target: Double,
    scheduler: Scheduler,
    targetSavingGoals: List<BudgetSavingGoal>
    ): Entity(id=id) {

    var target: Double by Delegates.observable(target) { _, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var scheduler: Scheduler by Delegates.observable(scheduler) { _, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var targetSavingGoals: List<BudgetSavingGoal> by Delegates.observable(targetSavingGoals.toList()) { _, old, new ->
        if (old != new)
            this.markHasChanged()
    }
}
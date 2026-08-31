package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.value_objects.SchedulerRecurrence
import java.time.LocalDate
import java.util.UUID

class SpendingPeriodTemplate(
    id: UUID = UUID.randomUUID(),
    recurrence: SchedulerRecurrence,
    isActive: Boolean = true,
    endDate: LocalDate? = null
): Entity(id) {
    var recurrence by cleanObservable(recurrence, this)
    var isActive by cleanObservable(isActive, this)
    var endDate by cleanObservable(endDate, this)
}
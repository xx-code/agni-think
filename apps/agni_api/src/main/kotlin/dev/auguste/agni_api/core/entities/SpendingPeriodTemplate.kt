package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.value_objects.SchedulerRecurrence
import java.time.LocalDate
import java.util.UUID

class SpendingPeriodTemplate(
    id: UUID = UUID.randomUUID(),
    startDate: LocalDate,
    recurrence: SchedulerRecurrence,
    targetBudgetIds: Set<UUID> = setOf(),
    isActive: Boolean = false,
    endDate: LocalDate? = null
): Entity(id) {
    var startDate by cleanObservable(startDate, this)
    var recurrence by cleanObservable(recurrence, this)
    var isActive by cleanObservable(isActive, this)
    var endDate by cleanObservable(endDate, this)
    var targetBudgetIds by cleanObservable(targetBudgetIds, this)

    fun checkIsActive(date: LocalDate = LocalDate.now()): Boolean {
        if (endDate != null && date >= endDate) {
            return false
        }
        return isActive
    }
}
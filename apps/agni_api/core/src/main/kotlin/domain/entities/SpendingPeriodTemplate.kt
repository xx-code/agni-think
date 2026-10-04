package domain.entities

import domain.exceptions.ValidationException
import domain.value_objects.SchedulerRecurrence
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
    var startDate: LocalDate by cleanObservable(startDate, this, {
        this.endDate == null || it < this.endDate
    }) {
        ValidationException.SpendingPeriodTemplateStartDateMustBeLesserThanEndDate(it, this.endDate)
    }
    var recurrence by cleanObservable(recurrence, this)
    var isActive by cleanObservable(isActive, this)
    var endDate: LocalDate? by cleanObservable(endDate, this, {
        (it != null && it > this.startDate) || it == null
    }) {
        ValidationException.SpendingPeriodTemplateEndDateMustBeGreaterThanStartDate(this.startDate, it)
    }
    var targetBudgetIds by cleanObservable(targetBudgetIds, this)

    fun checkIsActive(date: LocalDate = LocalDate.now()): Boolean {
        if (endDate != null && date >= endDate) {
            return false
        }
        return isActive
    }
}
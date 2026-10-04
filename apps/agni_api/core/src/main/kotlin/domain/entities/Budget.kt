package domain.entities

import domain.exceptions.ValidationException
import domain.value_objects.Scheduler
import java.time.LocalDateTime
import java.util.UUID


class Budget(
    id: UUID = UUID.randomUUID(),
    title: String,
    target: Double,
    scheduler: Scheduler,
    isArchived: Boolean = false,
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
    ): Entity(id=id, createdAt = createdAt, updatedAt = updatedAt) {

    var title: String by cleanObservable(title, this)

    var target: Double by cleanObservable(target, this, {
        it > 0.0
    }) {
        ValidationException.InvalidBudgetTarget(it)
    }

    var scheduler: Scheduler by cleanObservable(scheduler, this)

    var isArchived: Boolean by cleanObservable(isArchived, this)
}
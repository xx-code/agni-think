package domain.entities

import domain.enums.PatrimonySnapshotStatusType
import java.time.LocalDate
import java.util.UUID


class PatrimonySnapshot(
    id: UUID = UUID.randomUUID(),
    val patrimonyId: UUID,
    date: LocalDate = LocalDate.now(),
    currentBalanceObserved: Double,
    status: PatrimonySnapshotStatusType
    ): Entity(id = id) {

    var date by cleanObservable(date, this)

    var currentBalanceObserved by cleanObservable(currentBalanceObserved, this)

    var status by cleanObservable(status, this)
}
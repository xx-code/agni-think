package dev.auguste.agni_api.core.entities

import java.time.LocalDate
import java.util.UUID

class FinanceReport(
    id: UUID = UUID.randomUUID(),
    title: String,
    description: String,
    date: LocalDate = LocalDate.now(),
): Entity(id) {
    var title by cleanObservable(title, this)
    var description by cleanObservable(description, this)
    var date by cleanObservable(date, this)
}
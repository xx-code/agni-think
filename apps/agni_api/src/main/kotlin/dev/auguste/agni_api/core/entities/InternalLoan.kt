package dev.auguste.agni_api.core.entities

import java.time.LocalDate
import java.util.UUID

class InternalLoan(
    id: UUID = UUID.randomUUID(),
    val creditTargetId: UUID,
    val invoiceId: UUID,
    fundSourceId: UUID,
    dueDate: LocalDate,
): Entity(id) {
    var fundSourceId by cleanObservable(fundSourceId, this)
    var dueDate by cleanObservable(dueDate, this)
}
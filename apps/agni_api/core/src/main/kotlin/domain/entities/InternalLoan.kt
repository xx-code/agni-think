package domain.entities

import java.time.LocalDate
import java.util.UUID


class InternalLoan(
    id: UUID = UUID.randomUUID(),
    val creditTargetId: UUID,
    val invoiceId: UUID,
    fundSourceId: UUID,
    dueDate: LocalDate,
    trackRefunds: Set<UUID> = setOf()
): Entity(id) {
    var fundSourceId by cleanObservable(fundSourceId, this)
    var trackRefunds by cleanObservable(trackRefunds, this)
    var dueDate by cleanObservable(dueDate, this)
}
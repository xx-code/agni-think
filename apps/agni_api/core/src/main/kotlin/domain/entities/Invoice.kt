package domain.entities

import domain.value_objects.InvoiceDeduction
import domain.value_objects.InvoiceModuleLinker
import java.time.LocalDateTime
import java.util.UUID
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import domain.enums.InvoiceType


class Invoice(
    id: UUID = UUID.randomUUID(),
    accountId: UUID,
    status: InvoiceStatusType,
    movement: InvoiceMovementType,
    type: InvoiceType,
    deductions: MutableSet<InvoiceDeduction> = mutableSetOf(),
    date: LocalDateTime = LocalDateTime.now(),
    isFreeze: Boolean = false,
    moduleLinkers: MutableList<InvoiceModuleLinker> = mutableListOf(),
    ): Entity(id = id) {
    var accountId: UUID by cleanObservable(accountId, this)

    var status: InvoiceStatusType by cleanObservable(status, this)

    var date by cleanObservable(date, this)

    var isFreeze: Boolean by cleanObservable(isFreeze, this)

    var type: InvoiceType by cleanObservable(type, this)

    var movement:InvoiceMovementType by cleanObservable(movement, this)

    var deductions by cleanObservable(deductions, this)
    
    var moduleLinkers by cleanObservable(moduleLinkers, this)
}
package domain.entities

import domain.enums.DeductionBaseType
import domain.enums.DeductionModeType
import java.util.UUID


class Deduction(
    id: UUID = UUID.randomUUID(),
    title: String,
    val base: DeductionBaseType,
    val mode: DeductionModeType,
    description: String,
): Entity(id = id) {
    var title: String by cleanObservable(title, this)
    var description: String by cleanObservable(description, this)
}
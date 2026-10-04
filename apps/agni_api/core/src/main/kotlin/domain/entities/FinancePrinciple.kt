package domain.entities

import domain.enums.PrincipleType
import domain.exceptions.ValidationException
import java.util.UUID


class FinancePrinciple(
    id: UUID = UUID.randomUUID(),
    name: String,
    description: String,
    targetType: PrincipleType,
    strictness: Int, // 1 (soft) to 10 (hard),
    logicRules: String? = null
    ) : Entity(id) {
    var name by cleanObservable(name, this)

    var description by cleanObservable(description, this)

    var targetType by cleanObservable(targetType, this)

    var strictness by cleanObservable(strictness, this, {
        it in 1..10
    }) {
        ValidationException.InvalidFinancialPrincipleStrictness(it)
    }

    var logicRules by cleanObservable(logicRules, this)
}
package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.entities.enums.PrincipleType
import java.util.UUID
import kotlin.properties.Delegates

class FinancePrinciple(
    id: UUID = UUID.randomUUID(),
    name: String,
    description: String,
    targetType: PrincipleType,
    strictness: Int, // 1 (soft) to 10 (hard),
    logicRules: String? = null
    ) : Entity(id) {
    var name by Delegates.observable(name) {
       _, old, new ->
       if (old != new)
           markHasChanged()
   }

    var description by Delegates.observable(description) {
        _, old, new ->
        if (old != new)
            markHasChanged()
    }

    var targetType by Delegates.observable(targetType) {
        _, old, new ->
        if (old != new)
            markHasChanged()
    }

    var strictness by Delegates.observable(strictness) {
        _, old, new ->
        if (old != new)
            markHasChanged()
    }

    var logicRules by Delegates.observable(logicRules) {
        _, old, new ->
        if (old != new)
            markHasChanged()
    }
}
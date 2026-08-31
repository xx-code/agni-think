package dev.auguste.agni_api.core.entities

import java.util.UUID

class Profile(
    id: UUID = UUID.randomUUID(),
    maxWishlistAmount: Double = 0.0,
    fixSpendPercentage: Double = 0.0,
    varialSpendPercentage: Double = 0.0,
    savingPercentage: Double = 0.0
): Entity(id) {
    var maxWishlistAmount by cleanObservable(maxWishlistAmount, this)
    var fixSpendPercentage by cleanObservable(fixSpendPercentage, this)
    var varialSpendPercentage by cleanObservable(varialSpendPercentage, this)
    var savingPercentage by cleanObservable(savingPercentage, this)
}
package dev.auguste.agni_api.core.entities

import java.util.UUID

class Profile(
    id: UUID = UUID.randomUUID(),
    maxWishlistAmount: Double = 0.0,
    fixSpendPercentage: Double = 0.0,
    varialSpendPercentage: Double = 0.0,
    savingPercentage: Double = 0.0
): Entity(id) {
    var maxWishlistAmount by cleanObservable(maxWishlistAmount, this, {
        it >= 0.0
    }, DomainException.Validation.ProfileMaxWishlistAmountMustBePositif())
    var fixSpendPercentage by cleanObservable(fixSpendPercentage, this, {
        it in 0.0..100.0 && (it + varialSpendPercentage + savingPercentage) <= 100.0
    }, DomainException.Validation.ProfileRulePercentageMustBePositif("Fix", fixSpendPercentage, (fixSpendPercentage + savingPercentage + varialSpendPercentage)))
    var varialSpendPercentage by cleanObservable(varialSpendPercentage, this, {
        it in 0.0..100.0 && (it + fixSpendPercentage + savingPercentage) <= 100.0
    }, DomainException.Validation.ProfileRulePercentageMustBePositif("Fix", varialSpendPercentage, (fixSpendPercentage + savingPercentage + varialSpendPercentage)))
    var savingPercentage by cleanObservable(savingPercentage, this, {
        it in 0.0..100.0 && (it + fixSpendPercentage + varialSpendPercentage) <= 100.0
    }, DomainException.Validation.ProfileRulePercentageMustBePositif("Fix", savingPercentage, (fixSpendPercentage + savingPercentage + varialSpendPercentage)))
}
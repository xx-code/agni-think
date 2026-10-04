package domain.entities

import domain.exceptions.ValidationException

import java.util.UUID


class Profile(
    id: UUID = UUID.randomUUID(),
    maxWishlistAmount: Double = 0.0,
    fixSpendPercentage: Double = 0.0,
    varialSpendPercentage: Double = 0.0,
    savingPercentage: Double = 0.0,
    balanceBuffer: Double = 0.0,
): Entity(id) {
    var maxWishlistAmount: Double by cleanObservable(maxWishlistAmount, this, {
        it >= 0.0
    }) {
        ValidationException.ProfileMaxWishlistAmountMustBePositif()
    }
    var fixSpendPercentage: Double by cleanObservable(
        fixSpendPercentage,
        this,
        {
            it in 0.0..100.0 && (it + this.varialSpendPercentage + this.savingPercentage) <= 100.0
        },
    ) {
        ValidationException.ProfileRulePercentageMustBePositif(
            "Fix",
            it,
            it + this.varialSpendPercentage + this.savingPercentage
        )
    }
    var varialSpendPercentage: Double by cleanObservable(
        varialSpendPercentage,
        this,
        {
            it in 0.0..100.0 && (it + this.fixSpendPercentage + this.savingPercentage) <= 100.0
        },
    ) {
        ValidationException.ProfileRulePercentageMustBePositif(
            "Variable",
            it,
            it + this.fixSpendPercentage + this.savingPercentage
        )
    }
    var savingPercentage: Double by cleanObservable(
        savingPercentage,
        this,
        {
            it in 0.0..100.0 && (it + this.fixSpendPercentage + this.varialSpendPercentage) <= 100.0
        },
    ) {
        ValidationException.ProfileRulePercentageMustBePositif(
            "Saving",
            it,
            it + this.fixSpendPercentage + this.varialSpendPercentage
        )
    }
    var balanceBuffer: Double by cleanObservable(
        balanceBuffer,
        this,
        {
            it >= 0.0
        }
    ) {
        ValidationException.BalanceBufferMustBeGreaterOrEqualToZero(it)
    }
}
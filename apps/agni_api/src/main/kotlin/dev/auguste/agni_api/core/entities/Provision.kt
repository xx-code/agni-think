package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.entities.enums.DepreciationType
import dev.auguste.agni_api.core.entities.enums.ProvisionType
import dev.auguste.agni_api.core.value_objects.ProvisionDepreciateCriteria
import dev.auguste.agni_api.core.value_objects.ProvisionPayment
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.pow


class Provision(
    id: UUID = UUID.randomUUID(),
    title: String,
    initialCost: Double,
    isPatrimony: Boolean,
    acquisitionDate: LocalDate,
    expectedLifespanMonth: Int,
    depreciationCriteria: MutableList<ProvisionDepreciateCriteria>,
    floorValue: Double,
    val type: ProvisionType = ProvisionType.DEPRECIATE,
    paymentInfo: ProvisionPayment? = null,
    interestLoan: Double = 0.0,
    loanMonth: Long = 0,
): Entity(id = id) {

    var title by cleanObservable(title, this)
    var initialCost by cleanObservable(initialCost, this, {
        it > 0
    }, DomainException.BusinessLogic.Validation("Provisionable initial doesn't have a cost"))
    var acquisitionDate by cleanObservable(acquisitionDate, this)
    var expectedLifespanMonth by cleanObservable(expectedLifespanMonth, this)
    var isPatrimony by cleanObservable(isPatrimony, this)
    var floorValue by cleanObservable(floorValue, this)
    var depreciationCriteria by cleanObservable(depreciationCriteria, this)
    var interestLoan by cleanObservable(interestLoan, this,
        {
            it >= 0.0
        },
        DomainException.Validation.ProvisionDepreciateLoanInterestPositif(interestLoan)
    )
    var loanMonth by cleanObservable(loanMonth, this, {
        it > 0.0 && type == ProvisionType.DEPRECIATE_LOAN
    },
        DomainException.Validation.ProvisionDepreciateLoanMonthMustBeGreaterThanZero(loanMonth))

    var paymentInfo by cleanObservable(paymentInfo, this, {
        it != null && type == ProvisionType.DEPRECIATE_LOAN
    }, DomainException.BusinessLogic.ProvisionWithLoanMustHaveAScheduleInvoice())


    fun calculateTotalCost(): Double {
        if (type != ProvisionType.DEPRECIATE_LOAN)
            return initialCost

        if (interestLoan <= 0.0 || loanMonth <= 0) {
            return initialCost
        }

        val monthlyRate = (interestLoan / 100.0) / 12.0

        return initialCost * (1.0 + monthlyRate).pow(loanMonth.toDouble())
    }

    fun calculateMonthlyPayment(): Double {
        if (loanMonth.toInt() == 0)
            return 0.0
        return calculateTotalCost() / loanMonth
    }

    fun calculateTotalCostPerMonth(): Double {
        if (expectedLifespanMonth == 0)
            return 0.0

        val netDepreciationCost = calculateTotalCost() - calculateResidualValue()
        return (netDepreciationCost / expectedLifespanMonth).coerceAtLeast(0.0)
    }

    fun calculateResidualValue(date: LocalDate = LocalDate.now()): Double {
        val monthsOwned = ChronoUnit.MONTHS.between(acquisitionDate, date).coerceAtLeast(0)
        var residual = initialCost

        val decliningBalances = depreciationCriteria.filter {
            it.type == DepreciationType.DECLINING_BALANCE
        }.sortedBy { it.monthRange }

        var previousRange = 0L
        for (criteria in decliningBalances) {
            if (monthsOwned <= previousRange) break

            val monthsInCurrentBracket = (monthsOwned - previousRange)
                .coerceAtMost(criteria.monthRange.toLong() - previousRange)

            if (monthsInCurrentBracket > 0 && criteria.value > 0.0) {
                val annualRate = criteria.value / 100.0
                val monthlyFactor = 1.0 - (annualRate / 12.0)
                residual *= monthlyFactor.pow(monthsInCurrentBracket.toDouble())
            }
            previousRange = criteria.monthRange.toLong()
        }

        val straightLineCriteria = depreciationCriteria.filter {
            it.type == DepreciationType.STRAIGHT_LINE
        }

        if (straightLineCriteria.isNotEmpty()) {
            val totalStraightLineAnnualRate = straightLineCriteria.sumOf { it.value }
            if (totalStraightLineAnnualRate > 0.0) {
                val monthlyDepreciation = initialCost * ((totalStraightLineAnnualRate / 100.0) / 12.0)
                residual -= monthlyDepreciation * monthsOwned
            }
        }

        val fixedCriteria = depreciationCriteria.filter {
            it.type == DepreciationType.FIX || it.type == DepreciationType.FIX_PERCENTAGE
        }
        fixedCriteria.forEach { criteria ->
            if (criteria.type == DepreciationType.FIX_PERCENTAGE) {
                residual -= residual * (criteria.value / 100.0)
            } else {
                residual -= criteria.value
            }
        }

        return residual.coerceAtLeast(floorValue)
    }

}
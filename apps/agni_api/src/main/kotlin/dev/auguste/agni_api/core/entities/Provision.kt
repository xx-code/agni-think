package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.entities.enums.DepreciationType
import dev.auguste.agni_api.core.entities.enums.ProvisionType
import dev.auguste.agni_api.core.value_objects.ProvisionDepreciateCriteria
import dev.auguste.agni_api.core.value_objects.Scheduler
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.pow
import kotlin.properties.Delegates

class Provision(
    id: UUID = UUID.randomUUID(),
    title: String,
    initialCost: Double,
    isPatrimony: Boolean,
    acquisitionDate: LocalDate,
    expectedLifespanMonth: Int,
    depreciationCriteria: List<ProvisionDepreciateCriteria>,
    floorValue: Double,
    type: ProvisionType = ProvisionType.DEPRECIATE,
    interestLoan: Double = 0.0,
    loanMonth: Long = 0,
    scheduler: Scheduler? = null
): Entity(id = id) {

    var title by cleanObservable(title, this)
    var initialCost by cleanObservable(initialCost, this)
    var acquisitionDate by cleanObservable(acquisitionDate, this)
    var expectedLifespanMonth by cleanObservable(expectedLifespanMonth, this)
    var isPatrimony by cleanObservable(isPatrimony, this)
    var floorValue by cleanObservable(floorValue, this)
    var depreciationCriteria by cleanObservable(depreciationCriteria, this)
    var type by cleanObservable(type, this)
    var interestLoan by cleanObservable(interestLoan, this,
        {
            it >= 0.0
        },
        DomainException.Validation.ProvisionDepreciateLoanInterestPositif(interestLoan)
    )
    var loanMonth by cleanObservable(loanMonth, this, {
        it > 0.0
    },
        DomainException.Validation.ProvisionDepreciateLoanMonthMustBeGreaterThanZero(loanMonth))

    var scheduler by cleanObservable(scheduler, this, {
        it != null &&  type == ProvisionType.DEPRECIATE_LOAN
    }, DomainException.Validation.ProvisionWithLoanMustHaveAScheduler())

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

    fun calculateResidualValue(): Double {
        val monthsOwned = ChronoUnit.MONTHS.between(acquisitionDate, LocalDate.now()).coerceAtLeast(0)
        var residual = initialCost

        val percentageCriteria = depreciationCriteria.filter {
            it.type == DepreciationType.DECLINING_BALANCE || it.type == DepreciationType.STRAIGHT_LINE
        }.sortedBy { it.monthRange }

        percentageCriteria.forEach { criteria ->
            when (criteria.type) {
                DepreciationType.DECLINING_BALANCE -> {
                    // Nombre de mois applicables pour cette tranche
                    val applicableMonths = monthsOwned.coerceAtMost(criteria.monthRange.toLong()).toDouble()
                    if (applicableMonths > 0 && criteria.value > 0.0) {
                        // Conversion du taux annuel en facteur d'amortissement mensuel
                        val annualRate = criteria.value / 100.0
                        val monthlyFactor = 1.0 - (annualRate / 12.0)
                        residual *= monthlyFactor.pow(applicableMonths)
                    }
                }
                DepreciationType.STRAIGHT_LINE -> {
                    if (criteria.value > 0.0) {
                        val monthlyDepreciation = initialCost * ((criteria.value / 100.0) / 12.0)
                        residual -= monthlyDepreciation * monthsOwned
                    }
                }
                else -> {}
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
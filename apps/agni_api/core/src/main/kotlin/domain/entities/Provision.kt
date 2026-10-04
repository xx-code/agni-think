package domain.entities

import domain.exceptions.ValidationException

import domain.enums.DepreciationType
import domain.enums.ProvisionType
import domain.value_objects.ProvisionDepreciateCriteria
import domain.value_objects.ProvisionPayment
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.pow



class Provision(
    id: UUID = UUID.randomUUID(),
    title: String,
    costHT: Double,
    costTTC: Double,
    isInstallmentOnTTC: Boolean = true,
    isPatrimony: Boolean,
    acquisitionDate: LocalDate,
    expectedLifespanMonth: Int,
    depreciationCriteria: MutableList<ProvisionDepreciateCriteria>,
    floorValue: Double,
    val type: ProvisionType = ProvisionType.DEPRECIATE,
    fundAmortizationId: UUID? = null,
    paymentInfo: ProvisionPayment? = null,
    interestLoan: Double = 0.0,
    loanMonth: Long = 0,
): Entity(id = id) {

    var title by cleanObservable(title, this)
    var costHT: Double by cleanObservable(
        costHT,
        this,
        {
            it > 0
        }
    ) {
        ValidationException.ProvisionInitialMustHaveCost()
    }
    var costTTC: Double by cleanObservable(
        costTTC,
        this,
        {
            it > 0
        }
    ) {
        ValidationException.ProvisionInitialMustHaveCost()
    }
    var acquisitionDate by cleanObservable(acquisitionDate, this)
    var expectedLifespanMonth by cleanObservable(expectedLifespanMonth, this)
    var isPatrimony by cleanObservable(isPatrimony, this)
    var floorValue by cleanObservable(floorValue, this)
    var depreciationCriteria by cleanObservable(depreciationCriteria, this)
    var interestLoan: Double by cleanObservable(
        interestLoan, this,
        {
            it >= 0.0
        }
    ) {
        ValidationException.ProvisionDepreciateLoanInterestPositif(it)
    }
    var loanMonth: Long by cleanObservable(
        loanMonth, this, {
            it > 0.0 && this.type == ProvisionType.DEPRECIATE_LOAN
        }
    ) {
        ValidationException.ProvisionDepreciateLoanMonthMustBeGreaterThanZero(it)
    }

    var paymentInfo: ProvisionPayment? by cleanObservable(paymentInfo, this, {
        it != null && this.type == ProvisionType.DEPRECIATE_LOAN
    }) {
        ValidationException.ProvisionWithLoanMustHaveAScheduleInvoice()
    }

    var fundAmortizationId: UUID? by cleanObservable(
        fundAmortizationId,
        this,
        {
            it != null && this.type == ProvisionType.DEPRECIATE_LOAN
        }
    ) {
        ValidationException.ProvisionWithoutLoanMustNotHaveFundAmortization()
    }
    var isInstallmentOnTTC by cleanObservable(isInstallmentOnTTC, this)

    fun isAmortize(): Boolean  = fundAmortizationId != null && type == ProvisionType.DEPRECIATE_LOAN

    fun calculateTotalCost(): Double {
        if (type != ProvisionType.DEPRECIATE_LOAN)
            return costHT

        if (interestLoan <= 0.0 || loanMonth <= 0) {
            return costTTC
        }

        val n = loanMonth.toDouble()

        return calculateMonthlyPayment() * n
    }

    fun calculateMonthlyPayment(): Double {
        var cost = costHT
        if (isInstallmentOnTTC)
            cost = costTTC

        if (type != ProvisionType.DEPRECIATE_LOAN || interestLoan <= 0.0 || loanMonth <= 0)
            return cost / loanMonth.coerceAtLeast(1)

        val monthlyRate = (interestLoan / 100.0) / 12.0
        val n = loanMonth.toDouble()
        return cost * monthlyRate / (1.0 - (1.0 + monthlyRate).pow(-n))
    }

    fun calculateTotalCostPerMonth(): Double {
        if (expectedLifespanMonth == 0)
            return 0.0

        val netDepreciationCost = calculateTotalCost() - calculateResidualValue()
        return (netDepreciationCost / expectedLifespanMonth).coerceAtLeast(0.0)
    }

    fun calculateResidualValue(date: LocalDate = LocalDate.now()): Double {
        val monthsOwned = ChronoUnit.MONTHS.between(acquisitionDate, date).coerceAtLeast(0)
        var residual = costHT

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
                val monthlyDepreciation = costHT * ((totalStraightLineAnnualRate / 100.0) / 12.0)
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
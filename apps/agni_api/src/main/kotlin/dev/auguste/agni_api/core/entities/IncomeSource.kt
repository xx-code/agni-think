package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.entities.enums.IncomeSourceFrequencyType
import dev.auguste.agni_api.core.entities.enums.IncomeSourceType
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.properties.Delegates

class IncomeSource(
    id: UUID = UUID.randomUUID(),
    title: String,
    type: IncomeSourceType,
    payFrequency: IncomeSourceFrequencyType,
    reliabilityLevel: Int, // 0-100
    startDate: LocalDate,
    taxRate: Double = 0.0,
    otherRate: Double = 0.0,
    linkedAccountId: UUID? = null,
    annualGrossAmount: Double? = null,
    endDate: LocalDate? = null,
) : Entity(id) {

    var title: String by Delegates.observable(title) {
        _, old, new ->
        if (old != new)
            markHasChanged()
    }

    var type by Delegates.observable(type) { _, old, new ->
        if (old != new)
            markHasChanged()
    }

    var payFrequency by Delegates.observable(payFrequency) { _, old, new ->
        if (old != new)
            markHasChanged()
    }

    var reliabilityLevel by Delegates.observable(reliabilityLevel) { _, old, new ->
        if (old != new)
            markHasChanged()
    }

    var taxRate by Delegates.observable(taxRate) { _, old, new ->
        if (old != new)
            markHasChanged()
    }

    var otherRate by Delegates.observable(otherRate) { _, old, new ->
        if (old != new)
            markHasChanged()
    }

    var linkedAccountId by Delegates.observable(linkedAccountId) { _, old, new ->
        if (old != new)
            markHasChanged()
    }

    var annualGrossAmount by Delegates.observable(annualGrossAmount) { _, old, new ->
        if (old != new)
            markHasChanged()
    }

    var startDate by Delegates.observable(startDate) { _, old, new ->
        if (old != new)
            markHasChanged()
    }

    var endDate by Delegates.observable(endDate) { _, old, new ->
        if (old != new)
            markHasChanged()
    }

    fun getEstimateFutureOccurrence() : Int {
        val now = LocalDate.now()

        if (payFrequency == IncomeSourceFrequencyType.IRRELEVANTLY || (endDate != null && endDate!!.isBefore(now))) {
            return 0
        }

        val endOfYear = LocalDate.of(now.year, 12, 31)
        val effectiveStopDate = if (endDate != null && endDate!!.isBefore(endOfYear)) endDate else endOfYear

        return when (payFrequency) {
            IncomeSourceFrequencyType.WEEKLY ->
                ChronoUnit.WEEKS.between(now, effectiveStopDate).toInt()

            IncomeSourceFrequencyType.BIWEEKLY ->
                (ChronoUnit.WEEKS.between(now, effectiveStopDate) / 2).toInt()

            IncomeSourceFrequencyType.MONTHLY ->
                ChronoUnit.MONTHS.between(now, effectiveStopDate).toInt()

            IncomeSourceFrequencyType.YEARLY ->
                if (now.isBefore(effectiveStopDate)) 1 else 0

            else -> 0
        }
    }

    fun getEstimateNextNetAmount() : Double {
        if (payFrequency == IncomeSourceFrequencyType.IRRELEVANTLY || annualGrossAmount == null)
            return 0.0

        val tax = annualGrossAmount!! * (taxRate/100)
        val other = annualGrossAmount!! * (otherRate/100)

        val netAmount = annualGrossAmount!! - (tax + other)

        return netAmount / getEstimateNextNetAmount()
    }
}
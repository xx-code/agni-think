package domain.entities

import domain.enums.IncomeSourceFrequencyType
import domain.enums.PeriodType
import domain.value_objects.Scheduler
import domain.value_objects.SchedulerRecurrence
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import domain.enums.IncomeSourceType
import domain.exceptions.ValidationException


class IncomeSource(
    id: UUID = UUID.randomUUID(),
    title: String,
    type: IncomeSourceType,
    payFrequency: IncomeSourceFrequencyType,
    reliabilityLevel: Int,
    startDate: LocalDate,
    taxRate: Double = 0.0,
    otherRate: Double = 0.0,
    linkedAccountId: UUID? = null,
    annualGrossAmount: Double? = null,
    endDate: LocalDate? = null,
) : Entity(id) {

    var title: String by cleanObservable(title, this)

    var type by cleanObservable(type, this)

    var payFrequency by cleanObservable(payFrequency, this)

    var reliabilityLevel: Int by cleanObservable(reliabilityLevel, this, {
        it in 1..100
    }) {
        ValidationException.IncomeSourceReliabilityLevelInvalid(it)
    }

    var taxRate: Double by cleanObservable(taxRate, this, {
        it in 0.0..100.0 && it + this.otherRate <= 100.0
    }) {
        ValidationException.IncomeSourceTaxRateInvalid(it)
    }

    var otherRate: Double by cleanObservable(otherRate, this, {
        it in 0.0..100.0 && it + this.taxRate <= 100.0
    }) {
        ValidationException.IncomeSourceOtherRateInvalid(it)
    }

    var linkedAccountId by cleanObservable(linkedAccountId, this)

    var annualGrossAmount: Double? by cleanObservable(annualGrossAmount, this, {
        it == null || it >= 0.0
    }) {
        ValidationException.IncomeSourceAnnualGrossAmountMustBePositif(it)
    }

    var startDate: LocalDate by cleanObservable(startDate, this, {
        this.endDate == null || it < this.endDate
    }) {
        ValidationException.IncomeSourceStartDateMustLessThanEndDate(it, this.endDate)
    }

    var endDate: LocalDate? by cleanObservable(endDate, this, {
        it == null || it > this.startDate
    }) {
        ValidationException.IncomeSourceEndDateMustGreaterThanStartDate(this.startDate, it)
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

    fun getEstimateNextDate(date: LocalDateTime = LocalDateTime.now()): LocalDate {
        val recurrence = SchedulerRecurrence(
            period = when(payFrequency) {
                IncomeSourceFrequencyType.BIWEEKLY -> PeriodType.WEEK
                IncomeSourceFrequencyType.MONTHLY -> PeriodType.MONTH
                IncomeSourceFrequencyType.YEARLY -> PeriodType.YEAR
                else -> PeriodType.MONTH
            },
            interval = when(payFrequency) {
                IncomeSourceFrequencyType.BIWEEKLY -> 2
                else -> 1
            }
        )

        val scheduler = Scheduler(
            date = startDate.atStartOfDay(),
            repeater = recurrence
        )

        return scheduler.upgradeDate(date).toLocalDate()
    }

    fun getEstimateNextNetAmount() : Double {
        val gross = annualGrossAmount
        if (payFrequency == IncomeSourceFrequencyType.IRRELEVANTLY || gross == null)
            return 0.0


        val payOccurrences = when (payFrequency) {
            IncomeSourceFrequencyType.WEEKLY -> 52.0 // Number of week in year
            IncomeSourceFrequencyType.BIWEEKLY -> 26.0 // Number of biweek in a year
            IncomeSourceFrequencyType.MONTHLY -> 12.0 // Number of month in a year
            IncomeSourceFrequencyType.YEARLY -> 1.0
            IncomeSourceFrequencyType.IRRELEVANTLY -> return 0.0
        }

        val totalDeductionRate = (taxRate + otherRate) / 100.0
        val netAnnualAmount = gross * (1.0 - totalDeductionRate)

        return netAnnualAmount / payOccurrences
    }
}
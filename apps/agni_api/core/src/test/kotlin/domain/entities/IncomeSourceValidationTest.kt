package domain.entities

import kotlinx.coroutines.runBlocking

import domain.enums.IncomeSourceFrequencyType
import domain.enums.IncomeSourceType
import domain.exceptions.ValidationException
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class IncomeSourceValidationTest {

    private val startDate = LocalDate.of(2026, 1, 1)
    private val endDate = LocalDate.of(2026, 12, 31)

    private fun incomeSource(
        reliabilityLevel: Int = 80,
        taxRate: Double = 20.0,
        otherRate: Double = 5.0,
        annualGrossAmount: Double? = 50_000.0,
        startDate: LocalDate = this.startDate,
        endDate: LocalDate? = this.endDate,
    ) = IncomeSource(
        title = "Salary",
        type = IncomeSourceType.SALARY,
        payFrequency = IncomeSourceFrequencyType.MONTHLY,
        reliabilityLevel = reliabilityLevel,
        startDate = startDate,
        taxRate = taxRate,
        otherRate = otherRate,
        annualGrossAmount = annualGrossAmount,
        endDate = endDate,
    )

    @Test
    fun `accepts the reliability boundaries`() = runBlocking {
        val source = incomeSource()

        source.reliabilityLevel = 1
        assertEquals(1, source.reliabilityLevel)

        source.reliabilityLevel = 100
        assertEquals(100, source.reliabilityLevel)
    }

    @Test
    fun `refuses a reliability outside one to one hundred`() = runBlocking {
        val source = incomeSource()

        val tooLow = assertFailsWith<ValidationException.IncomeSourceReliabilityLevelInvalid> {
            source.reliabilityLevel = 0
        }
        assertEquals(mapOf("reliabilityLevel" to 0), tooLow.metadata)

        assertFailsWith<ValidationException.IncomeSourceReliabilityLevelInvalid> {
            source.reliabilityLevel = 101
        }
    }

    @Test
    fun `accepts rates that add up to one hundred`() = runBlocking {
        val source = incomeSource(taxRate = 0.0, otherRate = 0.0)

        source.taxRate = 60.0
        source.otherRate = 40.0

        assertEquals(60.0, source.taxRate)
        assertEquals(40.0, source.otherRate)
    }

    @Test
    fun `refuses a tax rate that would push the total above one hundred`() = runBlocking {
        val source = incomeSource(taxRate = 80.0, otherRate = 30.0)

        val error = assertFailsWith<ValidationException.IncomeSourceTaxRateInvalid> {
            source.taxRate = 90.0
        }

        assertEquals("INCOME_SOURCE_TAX_RATE_INVALID", error.errorKey)
        assertEquals(mapOf("taxRate" to 90.0), error.metadata)
    }

    @Test
    fun `refuses a negative tax rate`() = runBlocking {
        val source = incomeSource()

        assertFailsWith<ValidationException.IncomeSourceTaxRateInvalid> { source.taxRate = -0.5 }
    }

    @Test
    fun `refuses an other rate that would push the total above one hundred`() = runBlocking {
        val source = incomeSource(taxRate = 80.0, otherRate = 10.0)

        assertFailsWith<ValidationException.IncomeSourceOtherRateInvalid> { source.otherRate = 30.0 }
    }

    @Test
    fun `an other rate error reports the other rate and not the tax rate`() = runBlocking {
        val source = incomeSource(taxRate = 30.0, otherRate = 10.0)

        val error = assertFailsWith<ValidationException.IncomeSourceOtherRateInvalid> {
            source.otherRate = 80.0
        }

        assertEquals("INCOME_SOURCE_OTHER_RATE_INVALID", error.errorKey)
        assertEquals(mapOf("rate" to 80.0), error.metadata)
    }

    @Test
    fun `accepts a null or positive annual gross amount`() = runBlocking {
        val source = incomeSource()

        source.annualGrossAmount = null
        assertNull(source.annualGrossAmount)

        source.annualGrossAmount = 0.0
        assertEquals(0.0, source.annualGrossAmount)
    }

    @Test
    fun `refuses a negative annual gross amount`() = runBlocking {
        val source = incomeSource()

        val error = assertFailsWith<ValidationException.IncomeSourceAnnualGrossAmountMustBePositif> {
            source.annualGrossAmount = -1.0
        }

        assertEquals(mapOf("annualGrossAmount" to "-1.0"), error.metadata)
    }

    @Test
    fun `accepts an open ended income source`() = runBlocking {
        val source = incomeSource(endDate = null)

        source.endDate = null
        assertNull(source.endDate)
    }

    @Test
    fun `refuses a start date that is not before the end date`() = runBlocking {
        val source = incomeSource()

        val error = assertFailsWith<ValidationException.IncomeSourceStartDateMustLessThanEndDate> {
            source.startDate = endDate
        }

        assertEquals("INCOME_SOURCE_START_DATE_MUST_BE_LESS_THAN_END_DATE", error.errorKey)
        assertEquals(mapOf("startDate" to endDate, "endDate" to endDate), error.metadata)
    }

    @Test
    fun `refuses an end date that is not after the start date`() = runBlocking {
        val source = incomeSource()

        val error = assertFailsWith<ValidationException.IncomeSourceEndDateMustGreaterThanStartDate> {
            source.endDate = startDate
        }

        assertEquals(mapOf("startDate" to startDate, "endDate" to startDate), error.metadata)
    }

    @Test
    fun `refuses an end date set before the start date`() = runBlocking {
        val source = incomeSource()

        assertFailsWith<ValidationException.IncomeSourceEndDateMustGreaterThanStartDate> {
            source.endDate = startDate.minusDays(1)
        }
    }

    @Test
    fun `lets a bounded income source become open ended again`() = runBlocking {
        val source = incomeSource()

        source.endDate = null

        assertNull(source.endDate)
    }

    @Test
    fun `validates the start date against the current end date`() = runBlocking {
        val source = incomeSource(startDate = startDate, endDate = LocalDate.of(2026, 6, 30))

        source.endDate = LocalDate.of(2026, 12, 31)
        source.startDate = LocalDate.of(2026, 2, 1)

        assertEquals(LocalDate.of(2026, 2, 1), source.startDate)
    }

    @Test
    fun `moves the start date of an open ended income source`() = runBlocking {
        val source = incomeSource(endDate = null)

        source.startDate = startDate.plusMonths(3)

        assertEquals(startDate.plusMonths(3), source.startDate)
    }

    @Test
    fun `the tax rate validator sees the current other rate`() = runBlocking {
        val source = incomeSource(taxRate = 0.0, otherRate = 50.0)

        source.otherRate = 40.0
        source.taxRate = 60.0

        assertEquals(60.0, source.taxRate)
    }

    @Test
    fun `the other rate validator sees the current tax rate`() = runBlocking {
        val source = incomeSource(taxRate = 50.0, otherRate = 0.0)

        source.taxRate = 40.0
        source.otherRate = 60.0

        assertEquals(60.0, source.otherRate)
    }
}
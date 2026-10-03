package domain.entities

import kotlinx.coroutines.runBlocking

import domain.enums.SpendingPeriodStateType
import domain.exceptions.ValidationException
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SpendingPeriodValidationTest {

    private val start = LocalDate.of(2026, 1, 1)
    private val end = LocalDate.of(2026, 1, 31)

    private fun spendingPeriod(startDate: LocalDate = start, endDate: LocalDate = end) = SpendingPeriod(
        spendingPeriodTemplateId = UUID.randomUUID(),
        startDate = startDate,
        endDate = endDate,
        freeAmount = 150.0,
        savingRateTarget = 20.0,
        totalExpectedIncome = 3_000.0,
        totalExpectedExpenses = 1_500.0,
        state = SpendingPeriodStateType.DRAFT,
        wantSpendingItems = listOf(spendingItem()),
        snapshot = spendingPeriodSnapshot,
    )

    @Test
    fun `accepts a period that lasts at least one day`() = runBlocking {
        val period = spendingPeriod(startDate = start, endDate = start.plusDays(1))

        assertEquals(start.plusDays(1), period.endDate)
    }

    @Test
    fun `refuses moving the start date onto the end date`() = runBlocking {
        val period = spendingPeriod()

        val error = assertFailsWith<ValidationException.SpendingPeriodStartDateMustBeLesserThanEndDate> {
            period.startDate = end
        }

        assertEquals("SPENDING_PERIOD_START_DATE_MUST_BE_LESS_THAN_END_DATE", error.errorKey)
        assertEquals(mapOf("startDate" to end, "endDate" to end), error.metadata)
    }

    @Test
    fun `refuses moving the start date after the end date`() = runBlocking {
        val period = spendingPeriod()

        assertFailsWith<ValidationException.SpendingPeriodStartDateMustBeLesserThanEndDate> {
            period.startDate = end.plusDays(1)
        }
    }

    @Test
    fun `refuses moving the end date onto the start date`() = runBlocking {
        val period = spendingPeriod()

        val error = assertFailsWith<ValidationException.SpendingPeriodEndDateMustBeGreaterThanStartDate> {
            period.endDate = start
        }

        assertEquals("SPENDING_PERIOD_END_DATE_MUST_BE_GREATER_THAN_START_DATE", error.errorKey)
        assertEquals(mapOf("startDate" to start, "endDate" to start), error.metadata)
    }

    @Test
    fun `refuses moving the end date before the start date`() = runBlocking {
        val period = spendingPeriod()

        assertFailsWith<ValidationException.SpendingPeriodEndDateMustBeGreaterThanStartDate> {
            period.endDate = start.minusDays(1)
        }
    }

    @Test
    fun `accepts shifting both bounds while keeping the period ordered`() = runBlocking {
        val period = spendingPeriod()

        period.startDate = start.plusDays(7)
        period.endDate = end.plusDays(7)

        assertEquals(start.plusDays(7), period.startDate)
        assertEquals(end.plusDays(7), period.endDate)
    }
}
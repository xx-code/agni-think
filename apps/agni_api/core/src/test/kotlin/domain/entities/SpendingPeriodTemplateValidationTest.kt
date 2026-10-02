package domain.entities

import domain.exceptions.ValidationException
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SpendingPeriodTemplateValidationTest {

    private val start = LocalDate.of(2026, 1, 1)
    private val end = LocalDate.of(2026, 3, 31)

    private fun template(startDate: LocalDate = start, endDate: LocalDate? = end, isActive: Boolean = true) =
        SpendingPeriodTemplate(
            startDate = startDate,
            recurrence = monthlyScheduler().repeater!!,
            isActive = isActive,
            endDate = endDate,
        )

    @Test
    fun `accepts a template with and without an end date`() {
        val bounded = template()
        bounded.endDate = end.plusMonths(1)

        val openEnded = template(endDate = null)
        assertNull(openEnded.endDate)

        assertEquals(end.plusMonths(1), bounded.endDate)
    }

    @Test
    fun `refuses moving the start date onto the end date`() {
        val template = template()

        val error = assertFailsWith<ValidationException.SpendingPeriodTemplateStartDateMustBeLesserThanEndDate> {
            template.startDate = end
        }

        assertEquals("SPENDING_PERIOD_TEMPLATE_START_DATE_MUST_BE_LESS_THAN_END_DATE", error.errorKey)
        assertEquals(mapOf("startDate" to end, "endDate" to end.toString()), error.metadata)
    }

    @Test
    fun `refuses moving the start date after the end date`() {
        val template = template()

        assertFailsWith<ValidationException.SpendingPeriodTemplateStartDateMustBeLesserThanEndDate> {
            template.startDate = end.plusDays(1)
        }
    }

    @Test
    fun `refuses an end date on or before the start date`() {
        val template = template()

        val error = assertFailsWith<ValidationException.SpendingPeriodTemplateEndDateMustBeGreaterThanStartDate> {
            template.endDate = start
        }

        assertEquals("SPENDING_PERIOD_TEMPLATE_END_DATE_MUST_BE_GREATER_THAN_START_DATE", error.errorKey)
        assertEquals(mapOf("startDate" to start, "endDate" to start.toString()), error.metadata)

        assertFailsWith<ValidationException.SpendingPeriodTemplateEndDateMustBeGreaterThanStartDate> {
            template.endDate = start.minusDays(1)
        }
    }

    @Test
    fun `lets a bounded template become open ended again`() {
        val template = template()

        template.endDate = null

        assertNull(template.endDate)
    }

    @Test
    fun `moves the start date freely while the template is open ended`() {
        val template = template(endDate = null)

        template.startDate = start.plusMonths(6)

        assertEquals(start.plusMonths(6), template.startDate)
    }

    @Test
    fun `is inactive once the checked date reaches the end date`() {
        val template = template()

        assertTrue(template.checkIsActive(date = end.minusDays(1)))
        assertFalse(template.checkIsActive(date = end))
        assertFalse(template.checkIsActive(date = end.plusDays(1)))
    }

    @Test
    fun `an inactive template stays inactive`() {
        val template = template(isActive = false)

        assertFalse(template.checkIsActive(date = start))
    }
}
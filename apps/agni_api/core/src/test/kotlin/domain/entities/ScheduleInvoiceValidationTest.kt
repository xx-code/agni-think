package domain.entities

import domain.enums.InvoiceType
import domain.exceptions.ValidationException
import domain.value_objects.Scheduler
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ScheduleInvoiceValidationTest {

    private fun scheduleInvoice(
        amount: Double = 45.0,
        isFreeze: Boolean = false,
        freezeScheduler: Scheduler? = null,
    ) = ScheduleInvoice(
        title = "Rent",
        accountId = UUID.randomUUID(),
        type = InvoiceType.FIXED_COST,
        amount = amount,
        scheduler = monthlyScheduler(),
        categoryId = UUID.randomUUID(),
        moduleLinker = null,
        freezeScheduler = freezeScheduler,
        isFreeze = isFreeze,
    )

    @Test
    fun `accepts a strictly positive amount`() {
        val invoice = scheduleInvoice()

        invoice.amount = 0.01

        assertEquals(0.01, invoice.amount)
    }

    @Test
    fun `refuses a zero amount`() {
        val invoice = scheduleInvoice()

        val error = assertFailsWith<ValidationException.SchedulerInvoiceAmountShouldGreaterThanZero> {
            invoice.amount = 0.0
        }

        assertEquals("SCHEDULE_INVOICE_AMOUNT_SHOULD_BE_GREATER_THAN_ZERO", error.errorKey)
        assertEquals(mapOf("amount" to 0.0), error.metadata)
    }

    @Test
    fun `refuses a negative amount`() {
        val invoice = scheduleInvoice(amount = 45.0)

        val error = assertFailsWith<ValidationException.SchedulerInvoiceAmountShouldGreaterThanZero> {
            invoice.amount = -12.0
        }

        assertEquals(mapOf("amount" to -12.0), error.metadata)
    }

    @Test
    fun `refuses a freeze scheduler while the invoice is not frozen`() {
        val invoice = scheduleInvoice(isFreeze = false)

        val error = assertFailsWith<ValidationException.ScheduleFreezeInvoiceMustHaveAScheduler> {
            invoice.freezeScheduler = monthlyScheduler()
        }

        assertEquals("SCHEDULE_FREEZE_INVOICE_SCHEDULER_INVALID", error.errorKey)
    }

    @Test
    fun `refuses clearing the freeze scheduler`() {
        val invoice = scheduleInvoice(isFreeze = true, freezeScheduler = monthlyScheduler())

        assertFailsWith<ValidationException.ScheduleFreezeInvoiceMustHaveAScheduler> {
            invoice.freezeScheduler = null
        }
    }

    @Test
    fun `accepts a freeze scheduler once the invoice is frozen`() {
        val invoice = scheduleInvoice(isFreeze = false)

        invoice.isFreeze = true
        invoice.freezeScheduler = monthlyScheduler()

        assertEquals(LocalDate.of(2026, 1, 5), invoice.getFreezeEndDate())
    }

    @Test
    fun `reading the freeze end date of an unfrozen invoice is refused`() {
        val invoice = scheduleInvoice(isFreeze = false)

        assertFailsWith<ValidationException.ScheduleFreezeInvoiceMustHaveAScheduler> {
            invoice.getFreezeEndDate()
        }
    }
}
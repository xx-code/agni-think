package domain.entities

import domain.exceptions.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BudgetValidationTest {

    private fun budget(target: Double = 500.0) = Budget(
        title = "Groceries",
        target = target,
        scheduler = monthlyScheduler(),
    )

    @Test
    fun `accepts a strictly positive target`() {
        val budget = budget()

        budget.target = 0.01

        assertEquals(0.01, budget.target)
    }

    @Test
    fun `refuses a zero target`() {
        val budget = budget()

        val error = assertFailsWith<ValidationException.InvalidBudgetTarget> { budget.target = 0.0 }

        assertEquals("INVALID_BUDGET_TARGET", error.errorKey)
        assertEquals(mapOf("target" to 0.0), error.metadata)
    }

    @Test
    fun `refuses a negative target and reports the refused value`() {
        val budget = budget(target = 500.0)

        val error = assertFailsWith<ValidationException.InvalidBudgetTarget> { budget.target = -42.5 }

        assertEquals(mapOf("target" to -42.5), error.metadata)
    }
}
package domain.entities

import kotlinx.coroutines.runBlocking

import domain.exceptions.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

/**
 * The delegated properties built on `cleanObservable` only run their validator when the property is
 * assigned, never when it is initialised through the constructor. These tests pin that contract so
 * the per-entity suites can rely on it.
 */
class CleanObservableContractTest {

    @Test
    fun `constructor does not validate the initial value`() = runBlocking {
        val budget = Budget(
            title = "Groceries",
            target = -500.0,
            scheduler = monthlyScheduler(),
        )

        assertEquals(-500.0, budget.target)
    }

    @Test
    fun `a refused assignment keeps the previous value`() = runBlocking {
        val budget = Budget(
            title = "Groceries",
            target = 250.0,
            scheduler = monthlyScheduler(),
        )

        assertFailsWith<ValidationException.InvalidBudgetTarget> { budget.target = -1.0 }

        assertEquals(250.0, budget.target)
    }

    @Test
    fun `a refused assignment does not flag the entity as changed`() = runBlocking {
        val budget = Budget(
            title = "Groceries",
            target = 250.0,
            scheduler = monthlyScheduler(),
        )

        assertFailsWith<ValidationException.InvalidBudgetTarget> { budget.target = -1.0 }

        assertFalse(budget.hasChanged())
    }
}
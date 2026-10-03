package domain.entities

import kotlinx.coroutines.runBlocking

import domain.exceptions.ValidationException
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TransactionValidationTest {

    private fun transaction(amount: Double = 25.0) = Transaction(
        invoiceId = UUID.randomUUID(),
        categoryId = UUID.randomUUID(),
        amount = amount,
        description = "Groceries",
    )

    @Test
    fun `accepts a strictly positive amount`() = runBlocking {
        val transaction = transaction()

        transaction.amount = 0.01

        assertEquals(0.01, transaction.amount)
    }

    @Test
    fun `refuses a zero amount`() = runBlocking {
        val transaction = transaction()

        val error = assertFailsWith<ValidationException.TransactionAmountMustBeGreaterThanZero> {
            transaction.amount = 0.0
        }

        assertEquals("TRANSACTION_AMOUNT_MUST_BE_GREATER_THAN_ZERO", error.errorKey)
        assertEquals(mapOf("amount" to 0.0), error.metadata)
    }

    @Test
    fun `refuses a negative amount`() = runBlocking {
        val transaction = transaction(amount = 25.0)

        val error = assertFailsWith<ValidationException.TransactionAmountMustBeGreaterThanZero> {
            transaction.amount = -19.99
        }

        assertEquals(mapOf("amount" to -19.99), error.metadata)
    }

    @Test
    fun `a refused amount does not overwrite the current one`() = runBlocking {
        val transaction = transaction(amount = 25.0)

        assertFailsWith<ValidationException.TransactionAmountMustBeGreaterThanZero> {
            transaction.amount = -1.0
        }

        assertEquals(25.0, transaction.amount)
    }
}
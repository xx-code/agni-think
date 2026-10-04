package domain.entities

import kotlinx.coroutines.runBlocking

import domain.enums.FundType
import domain.exceptions.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FundValidationTest {

    private fun fund(target: Double = 3_000.0) = Fund(
        title = "Emergency fund",
        description = "Six months of expenses",
        target = target,
        balance = 1_200.0,
        type = FundType.EMERGENCY,
        accountId = null,
    )

    @Test
    fun `accepts a strictly positive target`() = runBlocking {
        val fund = fund()

        fund.target = 0.5

        assertEquals(0.5, fund.target)
    }

    @Test
    fun `refuses a zero target`() = runBlocking {
        val fund = fund()

        val error = assertFailsWith<ValidationException.FundTargetAmountMustGreaterThanZero> {
            fund.target = 0.0
        }

        assertEquals("FUND_TARGET_AMOUNT_MUST_BE_GREATER_THAN_ZERO", error.errorKey)
        assertEquals(mapOf("target" to 0.0), error.metadata)
    }

    @Test
    fun `refuses a negative target`() = runBlocking {
        val fund = fund(target = 3_000.0)

        val error = assertFailsWith<ValidationException.FundTargetAmountMustGreaterThanZero> {
            fund.target = -1.0
        }

        assertEquals(mapOf("target" to -1.0), error.metadata)
    }
}
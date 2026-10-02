package domain.entities

import domain.enums.PrincipleType
import domain.exceptions.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FinancePrincipleValidationTest {

    private fun principle(strictness: Int = 5) = FinancePrinciple(
        name = "Keep an emergency fund",
        description = "Six months of expenses",
        targetType = PrincipleType.EMERGENCY_FUND,
        strictness = strictness,
    )

    @Test
    fun `accepts the soft and hard strictness boundaries`() {
        val principle = principle()

        principle.strictness = 1
        assertEquals(1, principle.strictness)

        principle.strictness = 10
        assertEquals(10, principle.strictness)
    }

    @Test
    fun `refuses a strictness below one`() {
        val principle = principle()

        val error = assertFailsWith<ValidationException.InvalidFinancialPrincipleStrictness> {
            principle.strictness = 0
        }

        assertEquals("INVALID_FINANCIAL_PRINCIPLE_STRICTNESS", error.errorKey)
        assertEquals(mapOf("strictness" to 0), error.metadata)
    }

    @Test
    fun `refuses a strictness above ten`() {
        val principle = principle()

        val error = assertFailsWith<ValidationException.InvalidFinancialPrincipleStrictness> {
            principle.strictness = 11
        }

        assertEquals(mapOf("strictness" to 11), error.metadata)
    }
}
package domain.entities

import domain.exceptions.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProfileValidationTest {

    private fun profile(
        maxWishlistAmount: Double = 0.0,
        fixSpendPercentage: Double = 50.0,
        varialSpendPercentage: Double = 30.0,
        savingPercentage: Double = 20.0,
        balanceBuffer: Double = 0.0,
    ) = Profile(
        maxWishlistAmount = maxWishlistAmount,
        fixSpendPercentage = fixSpendPercentage,
        varialSpendPercentage = varialSpendPercentage,
        savingPercentage = savingPercentage,
        balanceBuffer = balanceBuffer,
    )

    @Test
    fun `accepts a zero max wishlist amount`() {
        val profile = profile(maxWishlistAmount = 0.0)

        profile.maxWishlistAmount = 250.0

        assertEquals(250.0, profile.maxWishlistAmount)
    }

    @Test
    fun `refuses a negative max wishlist amount`() {
        val profile = profile()

        val error = assertFailsWith<ValidationException.ProfileMaxWishlistAmountMustBePositif> {
            profile.maxWishlistAmount = -0.01
        }

        assertEquals("PROFILE_MAX_WISHLIST_MUST_BE_POSITIF", error.errorKey)
    }

    @Test
    fun `accepts percentages that add up to exactly one hundred`() {
        val profile = profile(fixSpendPercentage = 40.0, varialSpendPercentage = 30.0, savingPercentage = 20.0)

        profile.fixSpendPercentage = 50.0

        assertEquals(50.0, profile.fixSpendPercentage)
    }

    @Test
    fun `accepts percentages adding up to less than one hundred`() {
        val profile = profile()

        profile.fixSpendPercentage = 10.0
        profile.varialSpendPercentage = 10.0
        profile.savingPercentage = 10.0

        assertEquals(10.0, profile.savingPercentage)
    }

    @Test
    fun `refuses a fix percentage above one hundred`() {
        val profile = profile()

        assertFailsWith<ValidationException.ProfileRulePercentageMustBePositif> {
            profile.fixSpendPercentage = 100.5
        }
    }

    @Test
    fun `refuses a negative percentage`() {
        val profile = profile()

        assertFailsWith<ValidationException.ProfileRulePercentageMustBePositif> {
            profile.fixSpendPercentage = -1.0
        }
    }

    @Test
    fun `refuses a percentage pushing the total above one hundred`() {
        val profile = profile(fixSpendPercentage = 50.0, varialSpendPercentage = 30.0, savingPercentage = 20.0)

        val error = assertFailsWith<ValidationException.ProfileRulePercentageMustBePositif> {
            profile.savingPercentage = 30.0
        }

        assertEquals("PROFILE_BUDGET_RULE_PERCENTAGE_ERROR", error.errorKey)
        assertEquals(
            mapOf("rule" to "Saving", "percentage" to 30.0, "total" to 110.0),
            error.metadata,
        )
    }

    @Test
    fun `a variable percentage error is labelled as the variable rule`() {
        val profile = profile(fixSpendPercentage = 50.0, varialSpendPercentage = 30.0, savingPercentage = 20.0)

        val error = assertFailsWith<ValidationException.ProfileRulePercentageMustBePositif> {
            profile.varialSpendPercentage = 60.0
        }

        assertEquals("Variable", error.metadata["rule"])
        assertEquals(60.0, error.metadata["percentage"])
        assertEquals(130.0, error.metadata["total"])
    }

    @Test
    fun `a fix percentage error is labelled as the fix rule`() {
        val profile = profile(fixSpendPercentage = 50.0, varialSpendPercentage = 30.0, savingPercentage = 20.0)

        val error = assertFailsWith<ValidationException.ProfileRulePercentageMustBePositif> {
            profile.fixSpendPercentage = 60.0
        }

        assertEquals("Fix", error.metadata["rule"])
        assertEquals(110.0, error.metadata["total"])
    }

    @Test
    fun `the total reported by a percentage error uses the current sibling values`() {
        val profile = profile(fixSpendPercentage = 50.0, varialSpendPercentage = 30.0, savingPercentage = 20.0)

        profile.fixSpendPercentage = 40.0

        val error = assertFailsWith<ValidationException.ProfileRulePercentageMustBePositif> {
            profile.savingPercentage = 40.0
        }

        assertEquals(110.0, error.metadata["total"])
    }

    @Test
    fun `accepts a zero balance buffer`() {
        val profile = profile()

        profile.balanceBuffer = 0.0

        assertEquals(0.0, profile.balanceBuffer)
    }

    @Test
    fun `refuses a negative balance buffer`() {
        val profile = profile()

        val error = assertFailsWith<ValidationException.BalanceBufferMustBeGreaterOrEqualToZero> {
            profile.balanceBuffer = -100.0
        }

        assertEquals("BALANCE_BUFFER_MUST_BE_GREATER_OR_EQUAL_ZERO", error.errorKey)
        assertEquals(mapOf("value" to -100.0), error.metadata)
    }
}
package domain.enums

import domain.exceptions.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GoalEvaluationTypeTest {
    @Test
    fun `fromString parses value case insensitive`() {
        assertEquals(GoalEvaluationType.FUND, GoalEvaluationType.fromString("Fund"))
        assertEquals(GoalEvaluationType.TRANSACTION_TARGET, GoalEvaluationType.fromString("transactiontarget"))
        assertEquals(GoalEvaluationType.PATRIMONY, GoalEvaluationType.fromString("PATRIMONY"))
    }

    @Test
    fun `fromString throws for unknown value`() {
        assertFailsWith<ValidationException.BadType> {
            GoalEvaluationType.fromString("Unknown")
        }
    }
}

package domain.enums

import kotlinx.coroutines.runBlocking

import domain.exceptions.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GoalEvaluationTypeTest {
    @Test
    fun `fromString parses value case insensitive`() = runBlocking {
        assertEquals(GoalEvaluationType.FUND, GoalEvaluationType.fromString("Fund"))
        assertEquals(GoalEvaluationType.TRANSACTION_TARGET, GoalEvaluationType.fromString("transactiontarget"))
        assertEquals(GoalEvaluationType.PATRIMONY, GoalEvaluationType.fromString("PATRIMONY"))
    }

    @Test
    fun `fromString throws for unknown value`() = runBlocking {
        assertFailsWith<ValidationException.BadType> {
            GoalEvaluationType.fromString("Unknown")
        }
    }
}

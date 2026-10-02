package domain.enums

import domain.enums.GoalStatusType
import domain.exceptions.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GoalStatusTypeTest {

    @Test
    fun `fromInt maps by ordinal`() {
        assertEquals(GoalStatusType.ACTIVE, GoalStatusType.fromInt(0))
        assertEquals(GoalStatusType.COMPLETED, GoalStatusType.fromInt(1))
        assertEquals(GoalStatusType.PAUSED, GoalStatusType.fromInt(2))
    }

    @Test
    fun `fromInt throws for out of range values`() {
        assertFailsWith<ValidationException.BadType> { GoalStatusType.fromInt(-1) }
        assertFailsWith<ValidationException.BadType> { GoalStatusType.fromInt(3) }
        assertFailsWith<ValidationException.BadType> { GoalStatusType.fromInt(10) }
    }
}

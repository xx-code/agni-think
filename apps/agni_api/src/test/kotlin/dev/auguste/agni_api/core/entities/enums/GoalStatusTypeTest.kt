package dev.auguste.agni_api.core.entities.enums

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
        assertFailsWith<IllegalArgumentException> { GoalStatusType.fromInt(-1) }
        assertFailsWith<IllegalArgumentException> { GoalStatusType.fromInt(3) }
        assertFailsWith<IllegalArgumentException> { GoalStatusType.fromInt(10) }
    }
}

package persistences

import kotlinx.coroutines.runBlocking

import com.fasterxml.jackson.module.kotlin.readValue
import domain.enums.PeriodType
import domain.value_objects.Scheduler
import domain.value_objects.SchedulerRecurrence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class JacksonConfigTest {

    private val objectMapper = JacksonConfig().objectMapper()

    @Test
    fun `writes a due date as an ISO string and not as a timestamp array`() = runBlocking {
        val scheduler = Scheduler(LocalDateTime.of(2026, 10, 2, 23, 8))

        assertEquals("""{"due_date":"2026-10-02T23:08:00Z"}""", objectMapper.writeValueAsString(scheduler.toMap()))
    }

    @Test
    fun `writes the nested repeater with the canonical period value`() = runBlocking {
        val scheduler = Scheduler(
            date = LocalDateTime.of(2026, 10, 3, 5, 0),
            repeater = SchedulerRecurrence(PeriodType.DAY, 3)
        )

        assertEquals(
            """{"due_date":"2026-10-03T05:00:00Z","repeater":{"period":"Day","interval":3}}""",
            objectMapper.writeValueAsString(scheduler.toMap())
        )
    }

    @Test
    fun `serializes a period as its value and not as its enum name`() = runBlocking {
        val map = objectMapper.writeValueAsString(SchedulerRecurrence(PeriodType.MONTH, 1).toMap())

        assertEquals("""{"period":"Month","interval":1}""", map)
        assertFalse(map.contains("MONTH"), "the enum name must not leak into the json: $map")
    }

    @Test
    fun `reads back a scheduler written by the same mapper`() = runBlocking {
        val scheduler = Scheduler(
            date = LocalDateTime.of(2026, 10, 3, 5, 0),
            repeater = SchedulerRecurrence(PeriodType.WEEK, 2)
        )

        val restored = Scheduler.fromMap(objectMapper.readValue<Map<String, Any>>(objectMapper.writeValueAsString(scheduler.toMap())))

        assertEquals(scheduler.date, restored.date)
        assertEquals(PeriodType.WEEK, restored.repeater?.period)
        assertEquals(2, restored.repeater?.interval)
    }

    @Test
    fun `serializes a plain LocalDate as an ISO string`() = runBlocking {
        val model = JdbcScheduleInvoiceMapperTestModel(LocalDate.of(2026, 1, 31))

        assertEquals("""{"date":"2026-01-31"}""", objectMapper.writeValueAsString(model))
    }

    @Test
    fun `is tolerant to the casing already present in the database`() = runBlocking {
        val legacy = """{"due_date":"2026-10-03T05:00:00Z","repeater":{"period":"MONTH","interval":1}}"""

        val restored = Scheduler.fromMap(objectMapper.readValue<Map<String, Any>>(legacy))

        assertEquals(PeriodType.MONTH, restored.repeater?.period)
    }

    data class JdbcScheduleInvoiceMapperTestModel(val date: LocalDate)
}
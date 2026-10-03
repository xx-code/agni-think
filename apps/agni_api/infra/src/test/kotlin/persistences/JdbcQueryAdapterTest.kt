package persistences

import adapters.dto.QueryFilter
import adapters.dto.QuerySortBy
import adapters.repositories.IQueryExtendBuilder
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import domain.entities.Budget
import domain.entities.Entity
import domain.entities.Invoice
import domain.entities.ScheduleInvoice
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import persistences.jbdc_model.JdbcBudgetModel
import persistences.jbdc_model.JdbcBudgetModelMapper
import persistences.jbdc_model.JdbcInvoiceModel
import persistences.jbdc_model.JdbcInvoiceModelMapper
import persistences.jbdc_model.JdbcModel
import persistences.jbdc_model.JdbcScheduleInvoiceMapper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

/**
 * Regression tests on the predicates built by [JdbcQueryAdapter].
 *
 * A json extraction (`->>`) yields text, so it is casted to be comparable with a temporal value.
 * The bound parameter used to be stringified, which the driver sends as `character varying`, and
 * `timestamp with time zone <= character varying` does not exist in Postgres.
 */
class JdbcQueryAdapterTest {

    private val objectMapper: ObjectMapper = JacksonConfig().objectMapper()
    private val adapter = JdbcQueryAdapter(mockk<NamedParameterJdbcTemplate>(relaxed = true))

    private fun <M : JdbcModel, E : Entity> where(table: String, builder: IQueryExtendBuilder<E>, mapper: IMapper<M, E>): SqlQueryBuilder =
        adapter.getSqlStringBuilder(StringBuilder("SELECT * FROM $table WHERE 1=1"), builder, mapper)

    private fun invoiceMapper(fields: Map<String, String>): IMapper<JdbcInvoiceModel, Invoice> =
        mockk<IMapper<JdbcInvoiceModel, Invoice>>(relaxed = true).also {
            every { it.getEntityModelFieldName() } returns fields
            every { it.getSortField() } returns emptySet()
        }

    @Test
    fun `binds a temporal condition on a json field as timestamptz and not as a string`() {
        val mapper = JdbcBudgetModelMapper(objectMapper)
        val value = LocalDateTime.of(2026, 10, 2, 23, 8)

        val result = where(
            "budgets",
            QueryExtendBuilder<Budget>()
                .addCondition("scheduler.date", QueryComparator.LesserOrEquals, value),
            mapper
        )

        val sql = result.sql.toString()
        assertTrue(sql.contains("jsonb_exists(scheduler, 'due_date')"), "the json key guard must use the stored key: $sql")
        assertTrue(
            sql.contains("(scheduler->>'due_date')::timestamptz <= :scheduler_date::timestamptz"),
            "unexpected SQL: $sql"
        )

        val bound = result.params.getValue("scheduler_date")
        assertTrue(bound is OffsetDateTime, "a temporal condition must bind a temporal type, was ${bound?.javaClass?.name}")
        assertEquals(value.atOffset(ZoneOffset.UTC), bound)
    }

    @Test
    fun `binds a json date range on schedule transactions as timestamptz`() {
        val mapper = JdbcScheduleInvoiceMapper(objectMapper)
        val from = LocalDateTime.of(2026, 1, 1, 0, 0)
        val to = LocalDateTime.of(2026, 12, 31, 0, 0)

        val result = where(
            "schedule_transactions",
            QueryExtendBuilder<ScheduleInvoice>()
                .addCondition("scheduler.date", QueryComparator.GreaterOrEquals, from)
                .addCondition("scheduler.date", QueryComparator.Lesser, to),
            mapper
        )

        val sql = result.sql.toString()
        assertTrue(sql.contains("(scheduler->>'due_date')::timestamptz >= :scheduler_date::timestamptz"), "unexpected SQL: $sql")
        assertTrue(sql.contains("(scheduler->>'due_date')::timestamptz < :scheduler_date_1_::timestamptz"), "unexpected SQL: $sql")
        assertEquals(from.atOffset(ZoneOffset.UTC), result.params.getValue("scheduler_date"))
        assertEquals(to.atOffset(ZoneOffset.UTC), result.params.getValue("scheduler_date_1_"))
    }

    @Test
    fun `binds LocalDate and OffsetDateTime on plain columns as timestamptz`() {
        val mapper = invoiceMapper(mapOf("date" to "date"))

        val result = where(
            "transactions",
            QueryExtendBuilder<Invoice>()
                .addCondition("date", QueryComparator.GreaterOrEquals, LocalDate.of(2026, 1, 1))
                .addCondition("date", QueryComparator.LesserOrEquals, OffsetDateTime.of(2026, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC)),
            mapper
        )

        val sql = result.sql.toString()
        assertTrue(sql.contains("(date)::timestamptz >= :date::timestamptz"), "unexpected SQL: $sql")
        assertTrue(sql.contains("(date)::timestamptz <= :date_1_::timestamptz"), "unexpected SQL: $sql")
        assertEquals(LocalDate.of(2026, 1, 1).atStartOfDay().atOffset(ZoneOffset.UTC), result.params.getValue("date"))
        assertEquals(OffsetDateTime.of(2026, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC), result.params.getValue("date_1_"))
    }

    @Test
    fun `binds every element of an IN condition as a temporal value`() {
        val mapper = invoiceMapper(mapOf("date" to "date"))
        val dates = setOf(LocalDateTime.of(2026, 3, 1, 8, 0), LocalDateTime.of(2026, 3, 2, 8, 0))

        val result = where(
            "transactions",
            QueryExtendBuilder<Invoice>().addCondition("date", QueryComparator.In, dates),
            mapper
        )

        assertTrue(result.sql.toString().contains("(date)::timestamptz IN (:date)"), "unexpected SQL: ${result.sql}")

        @Suppress("UNCHECKED_CAST")
        val bound = result.params.getValue("date") as Collection<*>
        assertEquals(dates.map { it.atOffset(ZoneOffset.UTC) }, bound.toList())
    }

    @Test
    fun `never reuses a parameter name across conditions on the same field`() {
        val mapper = invoiceMapper(mapOf("date" to "date"))
        val lowerBound = LocalDateTime.of(2026, 1, 1, 0, 0)
        val inDates = setOf(LocalDateTime.of(2026, 3, 1, 8, 0))

        val result = where(
            "transactions",
            QueryExtendBuilder<Invoice>()
                .addCondition("date", QueryComparator.Greater, lowerBound)
                .addCondition("date", QueryComparator.Greater, LocalDateTime.of(2026, 1, 2, 0, 0))
                .addCondition("date", QueryComparator.In, inDates),
            mapper
        )

        val sql = result.sql.toString()
        assertTrue(sql.contains("> :date::timestamptz"), "unexpected SQL: $sql")
        assertTrue(sql.contains("> :date_1_::timestamptz"), "unexpected SQL: $sql")
        assertTrue(sql.contains("IN (:date_2_)"), "the IN condition must not reuse a name: $sql")

        assertEquals(lowerBound.atOffset(ZoneOffset.UTC), result.params.getValue("date"))
        assertEquals(LocalDateTime.of(2026, 1, 2, 0, 0).atOffset(ZoneOffset.UTC), result.params.getValue("date_1_"))

        @Suppress("UNCHECKED_CAST")
        assertEquals(inDates.map { it.atOffset(ZoneOffset.UTC) }, (result.params.getValue("date_2_") as Collection<*>).toList())
    }

    @Test
    fun `keeps the jsonb array containment predicate for json arrays`() {
        val mapper = JdbcInvoiceModelMapper(objectMapper)

        val result = where(
            "transactions",
            QueryExtendBuilder<Invoice>().addCondition("moduleLinkers.module", QueryComparator.Equal, "Housing"),
            mapper
        )

        assertTrue(
            result.sql.toString().contains("invoice_module_linkers @> jsonb_build_array(jsonb_build_object('module', :moduleLinkers_module))"),
            "unexpected SQL: ${result.sql}"
        )
    }

    @Test
    fun `turns a nested json path into valid jsonb traversals`() {
        val mapper = JdbcBudgetModelMapper(objectMapper)

        val result = where(
            "budgets",
            QueryExtendBuilder<Budget>().addCondition("scheduler.repeater", QueryComparator.In, setOf("Month", "Year")),
            mapper
        )

        val sql = result.sql.toString()
        assertTrue(
            sql.contains("scheduler->'repeater'->>'period'"),
            "expected a jsonb traversal on the nested path, got: $sql"
        )
        assertFalse(sql.contains("->>'repeater'->>"), "nested path must not chain two extractions: $sql")
        assertTrue(sql.contains("jsonb_exists(scheduler, 'repeater')"), "expected a guard on the first key: $sql")
    }

    @Test
    fun `casts a nested json date as timestamptz`() {
        val mapper = invoiceMapper(
            mapOf(
                "id" to "budget_id",
                "scheduler.repeater.startDate" to "scheduler->>'repeater'->>'startDate'"
            )
        )

        val result = where(
            "budgets",
            QueryExtendBuilder<Invoice>().addCondition(
                "scheduler.repeater.startDate",
                QueryComparator.Greater,
                LocalDateTime.of(2026, 1, 2, 0, 0)
            ),
            mapper
        )

        val sql = result.sql.toString()
        assertTrue(
            sql.contains("(scheduler->'repeater'->>'startDate')::timestamptz"),
            "unexpected SQL: $sql"
        )
        assertEquals(LocalDateTime.of(2026, 1, 2, 0, 0).atOffset(ZoneOffset.UTC), result.params.getValue("scheduler_repeater_startDate"))
    }

    @Test
    fun `keeps the conditions of every chained query builder when they share the params`() {
        val invoiceSide = invoiceMapper(mapOf("id" to "invoice_id", "isFreeze" to "is_freeze"))
        val transactionSide = invoiceMapper(mapOf("id" to "transaction_id", "date" to "date"))
        val params = MapSqlParameterSource()

        val first = adapter.getSqlStringBuilder(
            StringBuilder("SELECT * FROM transactions i WHERE 1=1"),
            QueryExtendBuilder<Invoice>().addCondition("isFreeze", QueryComparator.Equal, true),
            invoiceSide,
            "i",
            params
        )
        val second = adapter.getSqlStringBuilder(
            first.sql,
            QueryExtendBuilder<Invoice>().addCondition("date", QueryComparator.GreaterOrEquals, LocalDateTime.of(2026, 10, 1, 0, 0)),
            transactionSide,
            "t",
            params
        )

        val sql = second.sql.toString()
        assertTrue(sql.contains("i.is_freeze = :isFreeze"), "unexpected SQL: $sql")
        assertTrue(sql.contains("(t.date)::timestamptz >= :date::timestamptz"), "unexpected SQL: $sql")
        assertEquals(true, params.getValue("isFreeze"))
        assertEquals(LocalDateTime.of(2026, 10, 1, 0, 0).atOffset(ZoneOffset.UTC), params.getValue("date"))

        // un même nom de champ des deux côtés ne doit pas s'écraser
        val collisionParams = MapSqlParameterSource()
        val a = adapter.getSqlStringBuilder(
            StringBuilder("SELECT * FROM transactions i WHERE 1=1"),
            QueryExtendBuilder<Invoice>().addCondition("date", QueryComparator.Equal, 1),
            invoiceMapper(mapOf("date" to "date")),
            "i",
            collisionParams
        )
        val b = adapter.getSqlStringBuilder(
            a.sql,
            QueryExtendBuilder<Invoice>().addCondition("date", QueryComparator.Equal, 2),
            transactionSide,
            "t",
            collisionParams
        )

        assertEquals(1, collisionParams.getValue("date"))
        assertEquals(2, collisionParams.getValue("date_1_"))
        assertTrue(b.sql.toString().contains("i.date = :date AND t.date = :date_1_"), "unexpected SQL: ${b.sql}")
    }


    @Test
    fun `orders a distinct query by its distinct key and qualifies the sort field`() {
        val mapper = mockk<IMapper<JdbcInvoiceModel, Invoice>>(relaxed = true).also {
            every { it.getEntityModelFieldName() } returns mapOf("id" to "transaction_id", "date" to "date")
            every { it.getSortField() } returns setOf("date")
        }
        val params = MapSqlParameterSource()

        val sql = addPaginationSqlStringBuilder(
            StringBuilder("SELECT DISTINCT ON (i.transaction_id) i.* FROM transactions i WHERE 1=1"),
            params,
            QueryFilter(offset = 0, limit = 10, queryAll = false, sortBy = QuerySortBy("date", false)),
            mapper,
            true,
            "i"
        )

        assertEquals(
            "SELECT DISTINCT ON (i.transaction_id) i.* FROM transactions i WHERE 1=1 ORDER BY i.transaction_id, i.date DESC LIMIT :limit OFFSET :offset",
            sql.toString()
        )
        assertEquals(10, params.getValue("limit"))
        assertEquals(0, params.getValue("offset"))
    }


    @Test
    fun `tests membership in a json array of values with the containment operator`() {
        val budgetId = UUID.fromString("bae3f868-ba98-4d15-abd2-71ac48ea01c7")
        val otherId = UUID.fromString("9c02d239-9d56-4a7f-b8e5-34a47791c097")
        val mapper = invoiceMapper(mapOf("id" to "transaction_id", "budgetIds" to "jsonb_scalar_array:budget_ids"))

        val single = where(
            "records",
            QueryExtendBuilder<Invoice>().addCondition("budgetIds", QueryComparator.Equal, budgetId),
            mapper
        )
        assertTrue(
            single.sql.toString().contains("(budget_ids)::jsonb @> CAST(:budgetIds AS jsonb)"),
            "unexpected SQL: ${single.sql}"
        )
        assertEquals("\"$budgetId\"", single.params.getValue("budgetIds"))

        val included = where(
            "records",
            QueryExtendBuilder<Invoice>().addCondition("budgetIds", QueryComparator.In, setOf(budgetId, otherId)),
            mapper
        )
        assertTrue(
            included.sql.toString().contains(
                "(budget_ids)::jsonb @> ANY(ARRAY[CAST(:budgetIds_0_ AS jsonb), CAST(:budgetIds_1_ AS jsonb)])"
            ),
            "unexpected SQL: ${included.sql}"
        )
        assertEquals("\"$budgetId\"", included.params.getValue("budgetIds_0_"))
        assertEquals("\"$otherId\"", included.params.getValue("budgetIds_1_"))

        val excluded = where(
            "records",
            QueryExtendBuilder<Invoice>().addCondition("budgetIds", QueryComparator.NotIn, setOf(budgetId)),
            mapper
        )
        assertTrue(
            excluded.sql.toString().startsWith("SELECT * FROM records WHERE 1=1 AND NOT ((budget_ids)::jsonb @> ANY("),
            "unexpected SQL: ${excluded.sql}"
        )
    }


    @Test
    fun `keeps the json array convention when the query has an alias`() {
        val mapper = invoiceMapper(mapOf("id" to "record_id", "tagIds" to "jsonb_scalar_array:tag_ids"))
        val tagId = UUID.fromString("0a6e89f9-aff8-4033-8b82-12150664887e")

        val result = adapter.getSqlStringBuilder(
            StringBuilder("SELECT * FROM records i WHERE 1=1"),
            QueryExtendBuilder<Invoice>().addCondition("tagIds", QueryComparator.In, setOf(tagId)),
            mapper,
            "t"
        )

        assertTrue(
            result.sql.toString().contains("(t.tag_ids)::jsonb @> ANY(ARRAY[CAST(:tagIds_0_ AS jsonb)])"),
            "unexpected SQL: ${result.sql}"
        )
    }


    @Test
    fun `serializes json array values as json literals`() {
        val mapper = invoiceMapper(mapOf("id" to "record_id", "amount" to "jsonb_scalar_array:amounts"))

        val result = where(
            "records",
            QueryExtendBuilder<Invoice>().addCondition("amount", QueryComparator.In, setOf(12, 30)),
            mapper
        )

        assertEquals("12", result.params.getValue("amount_0_"))
        assertEquals("30", result.params.getValue("amount_1_"))
    }
}
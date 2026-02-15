package dev.auguste.agni_api.infras.persistences.query_adapters

import com.fasterxml.jackson.databind.ObjectMapper
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.ComparatorType
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryScheduleInvoiceExtend
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.infras.persistences.IMapper
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcScheduleInvoiceModel
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.util.UUID

@Component
class QueryScheduleInvoiceExtendJdbcAdapter(
    jdbcTemplate: NamedParameterJdbcTemplate,
    mapper: IMapper<JdbcScheduleInvoiceModel, ScheduleInvoice>,
    private val objectMapper: ObjectMapper
): BaseQueryExtendJdbcAdapter<JdbcScheduleInvoiceModel, ScheduleInvoice>(jdbcTemplate, mapper) {
    override fun getSqlQuery(): StringBuilder = StringBuilder("SELECT * FROM schedule_transactions WHERE 1=1")
    override fun getSqlCountQuery(): StringBuilder = StringBuilder("SELECT COUNT(*) FROM schedule_transactions WHERE 1=1")

    override fun getSqlStringBuilder(
        sqlBuilder: StringBuilder,
        queryFilter: QueryFilter,
        query: IQueryExtend<ScheduleInvoice>
    ): SqlQueryBuilder {
        val extend = query as QueryScheduleInvoiceExtend
        val params = MapSqlParameterSource()

        sqlBuilder.append(" AND jsonb_exists(scheduler, 'due_date')")
        val dateToVerify =extend.comparatorDueDate.date.atOffset(ZoneOffset.UTC).toString()

        val operator = when(extend.comparatorDueDate.comparator) {
            ComparatorType.Greater -> ">"
            ComparatorType.GreaterOrEquals -> ">="
            ComparatorType.Lesser -> "<"
            ComparatorType.LesserOrEquals -> "<="
            ComparatorType.Equal -> "="
        }

        sqlBuilder.append(" AND (scheduler->>'due_date')::timestamptz $operator :dueDate::timestamptz")
        params.addValue("dueDate", dateToVerify)

        return SqlQueryBuilder(sqlBuilder, params)
    }

    override fun getRawMapper(): RowMapper<JdbcScheduleInvoiceModel> {
        return  RowMapper { rs, _ ->
            JdbcScheduleInvoiceModel(
                id = rs.getObject("schedule_transaction_id", UUID::class.java),
                accountId = rs.getObject("account_id", UUID::class.java),
                categoryId = rs.getObject("category_id", UUID::class.java),
                amount = rs.getDouble("amount"),
                name = rs.getString("name"),
                type = rs.getString("type"),
                isPause = rs.getBoolean("is_pause"),
                isFreeze = rs.getBoolean("is_freeze"),
                scheduler = rs.getString("scheduler"),
                tagIds = rs.getString("tag_ids")?.let {
                    objectMapper.readValue(it, Array<String>::class.java).map { id -> UUID.fromString(id) }.toSet()
                } ?: emptySet()
            )
        }
    }
}
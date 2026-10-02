package dev.auguste.agni_api.infras.persistences.query_adapters

import com.fasterxml.jackson.databind.ObjectMapper
import adapters.dto.QueryFilter
import adapters.repositories.IQueryExtend
import adapters.repositories.query_extend.QueryComparator
import adapters.repositories.query_extend.QueryScheduleInvoiceExtend
import domain.entities.ScheduleInvoice
import dev.auguste.agni_api.infras.persistences.IMapper
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcScheduleInvoiceModel
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
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

        val comparatorDueDate = extend.comparatorDueDate
        if (comparatorDueDate != null) {
            sqlBuilder.append(" AND jsonb_exists(scheduler, 'due_date')")
            val dateToVerify = comparatorDueDate.date.atOffset(ZoneOffset.UTC).toString()

            val operator = when(comparatorDueDate.comparator) {
                QueryComparator.Greater -> ">"
                QueryComparator.GreaterOrEquals -> ">="
                QueryComparator.Lesser -> "<"
                QueryComparator.LesserOrEquals -> "<="
                QueryComparator.Equal -> "="
                else -> {}
            }

            sqlBuilder.append(" AND (scheduler->>'due_date')::timestamptz $operator :dueDate::timestamptz")
            params.addValue("dueDate", dateToVerify)
        }

        val comparatorEndDate = extend.comparatorEndDate
        if (comparatorEndDate != null) {

            val operator = when(comparatorEndDate.comparator) {
                QueryComparator.Greater -> ">"
                QueryComparator.GreaterOrEquals -> ">="
                QueryComparator.Lesser -> "<"
                QueryComparator.LesserOrEquals -> "<="
                QueryComparator.Equal -> "="
                else -> {}
            }

            sqlBuilder.append(" AND (end_date $operator :endDate OR end_date = NULL)")
            params.addValue("endDate",comparatorEndDate.date )
        }

        val type = extend.type
        if (type != null) {
            sqlBuilder.append(" AND LOWER(type) = :type")
            params.addValue("type", type.value.lowercase())
        }


        return SqlQueryBuilder(sqlBuilder, params)
    }

    override fun getRawMapper(): RowMapper<JdbcScheduleInvoiceModel> {
        return  RowMapper { rs, _ ->
            JdbcScheduleInvoiceModel(
                scheduleTransactionId = rs.getObject("schedule_transaction_id", UUID::class.java),
                accountId = rs.getObject("account_id", UUID::class.java),
                categoryId = rs.getObject("category_id", UUID::class.java),
                amount = rs.getDouble("amount"),
                name = rs.getString("name"),
                type = rs.getString("type"),
                isPause = rs.getBoolean("is_pause"),
                isFreeze = rs.getBoolean("is_freeze"),
                scheduler = rs.getString("scheduler"),
                tagIds = rs.getString("tag_ids"),
                endDate = rs.getObject("end_date", OffsetDateTime::class.java)?.toLocalDateTime(),
                moduleLinker = rs.getString("module_linker"),
                freezeScheduler = rs.getString("freeze_scheduler"),
            )
        }
    }
}
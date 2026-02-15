package dev.auguste.agni_api.infras.persistences.query_adapters

import com.fasterxml.jackson.databind.ObjectMapper
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.ComparatorType
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryBudgetExtend
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.infras.persistences.IMapper
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcBudgetModel
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.util.UUID


@Component
class QueryBudgetExtendJdbcAdapter(
    jdbcTemplate: NamedParameterJdbcTemplate,
    mapper: IMapper<JdbcBudgetModel, Budget>,
    private val objectMapper: ObjectMapper
) : BaseQueryExtendJdbcAdapter<JdbcBudgetModel, Budget>(jdbcTemplate, mapper) {

    override fun getSqlStringBuilder(
        sqlBuilder: StringBuilder,
        queryFilter: QueryFilter,
        query: IQueryExtend<Budget>,
    ): SqlQueryBuilder {
        val extend = query as QueryBudgetExtend
        val params = MapSqlParameterSource()

        sqlBuilder.append(" AND jsonb_exists(scheduler, 'due_date')")
        val dateToVerify =extend.scheduleDueDateComparator.date.atOffset(ZoneOffset.UTC).toString()

        val operator = when(extend.scheduleDueDateComparator.comparator) {
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

    override fun getSqlQuery(): StringBuilder = StringBuilder("SELECT * FROM budgets WHERE 1=1")
    override fun getSqlCountQuery(): StringBuilder = StringBuilder("SELECT COUNT(*) FROM budgets WHERE 1=1")

    override fun getRawMapper(): RowMapper<JdbcBudgetModel> {
        return RowMapper { rs, _ ->
            JdbcBudgetModel(
                id = rs.getObject("budget_id", UUID::class.java),
                name = rs.getString("title"),
                target = rs.getDouble("target"),
                scheduler = rs.getString("scheduler"),
                isArchived = rs.getBoolean("is_archived"),
                goalIds = rs.getString("save_goal_ids")?.let {
                    objectMapper.readValue(it, Array<String>::class.java).map { id -> UUID.fromString(id) }.toSet()
                } ?: emptySet(),
            )
        }
    }
}

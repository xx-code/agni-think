package dev.auguste.agni_api.infras.persistences.query_adapters

import adapters.dto.QueryFilter
import adapters.repositories.IQueryExtend
import adapters.repositories.query_extend.QueryComparator
import adapters.repositories.query_extend.QueryGoalExtend
import domain.entities.Goal
import dev.auguste.agni_api.infras.persistences.IMapper
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcGoalModel
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

@Component
class QueryGoalJdbcAdapter(
    jdbcTemplate: NamedParameterJdbcTemplate,
    mapper: IMapper<JdbcGoalModel, Goal>
) : BaseQueryExtendJdbcAdapter<JdbcGoalModel, Goal>(jdbcTemplate, mapper) {
    override fun getSqlQuery(): StringBuilder = StringBuilder("SELECT * FROM goals WHERE 1=1")

    override fun getSqlCountQuery(): StringBuilder = StringBuilder("SELECT COUNT(*) FROM goals WHERE 1=1")

    override fun getSqlStringBuilder(
        sqlBuilder: StringBuilder,
        queryFilter: QueryFilter,
        query: IQueryExtend<Goal>
    ): SqlQueryBuilder {
        val extend = query as QueryGoalExtend
        val params = MapSqlParameterSource()

        if (!extend.sourceIds.isNullOrEmpty()) {
            sqlBuilder.append(" AND source_id IN (:sourceIds)")
            params.addValue("sourceIds", extend.sourceIds)
        }

        val status = extend.status
        if (status != null) {
            sqlBuilder.append(" AND status = :status")
            params.addValue("status", status.ordinal)
        }

        val type = extend.type
        if (type != null) {
            sqlBuilder.append(" AND type = :type")
            params.addValue("type", type.value)
        }

        val dueDateComparator = extend.dueDateComparator
        if (dueDateComparator != null) {
            // sqlBuilder.append(" AND jsonb_exists(scheduler, 'due_date')")
            val dateToVerify = dueDateComparator.date.atOffset(ZoneOffset.UTC).toString()

            val operator = when(dueDateComparator.comparator) {
                QueryComparator.Greater -> ">"
                QueryComparator.GreaterOrEquals -> ">="
                QueryComparator.Lesser -> "<"
                QueryComparator.LesserOrEquals -> "<="
                QueryComparator.Equal -> "="
                else -> {}
            }

            sqlBuilder.append(" AND due_date::timestamptz $operator :dueDate::timestamptz")
            params.addValue("dueDate", dateToVerify)
        }

        return SqlQueryBuilder(sqlBuilder, params)
    }

    override fun getRawMapper(): RowMapper<JdbcGoalModel> {
        return RowMapper { rs, _ ->
            JdbcGoalModel(
                id = rs.getObject("goal_id", UUID::class.java),
                title = rs.getString("title"),
                sourceId = rs.getObject("source_id", UUID::class.java),
                description = rs.getString("description"),
                dueDate = rs.getObject("due_date", LocalDate::class.java),
                targetAmount = rs.getDouble("target_amount"),
                status = rs.getInt("status"),
                type = rs.getString("type")
            )
        }
    }
}
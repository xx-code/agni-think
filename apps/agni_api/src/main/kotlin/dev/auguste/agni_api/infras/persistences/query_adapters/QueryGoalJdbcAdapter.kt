package dev.auguste.agni_api.infras.persistences.query_adapters

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.ComparatorType
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryGoalExtend
import dev.auguste.agni_api.core.entities.Goal
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

        if (extend.sourceId != null) {
            sqlBuilder.append(" AND source_id = :sourceId")
            params.addValue("sourceId", extend.sourceId)
        }

        if (extend.status != null) {
            sqlBuilder.append(" AND status = :status")
            params.addValue("status", extend.status)
        }

        if (extend.type != null) {
            sqlBuilder.append(" AND type = :type")
            params.addValue("type", extend.type)
        }

        if (extend.dueDateComparator != null) {
            // sqlBuilder.append(" AND jsonb_exists(scheduler, 'due_date')")
            val dateToVerify = extend.dueDateComparator.date.atOffset(ZoneOffset.UTC).toString()

            val operator = when(extend.dueDateComparator.comparator) {
                ComparatorType.Greater -> ">"
                ComparatorType.GreaterOrEquals -> ">="
                ComparatorType.Lesser -> "<"
                ComparatorType.LesserOrEquals -> "<="
                ComparatorType.Equal -> "="
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
                title = rs.getObject("title", String::class.java),
                sourceId = rs.getObject("source_id", UUID::class.java),
                description = rs.getObject("description", String::class.java),
                dueDate = rs.getObject("due_date", LocalDate::class.java),
                targetAmount = rs.getObject("target_amount", Double::class.java),
                status = rs.getObject("status", Int::class.java),
                type = rs.getObject("type", String::class.java),
            )
        }
    }
}
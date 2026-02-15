package dev.auguste.agni_api.infras.persistences.query_adapters

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QuerySavingGoalExtend
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.infras.persistences.IMapper
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcSavingGoalModel
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID

@Component
class QuerySavingGoalExtendJdbcAdapter(
    jdbcTemplate: NamedParameterJdbcTemplate,
    mapper: IMapper<JdbcSavingGoalModel, SavingGoal>
): BaseQueryExtendJdbcAdapter<JdbcSavingGoalModel, SavingGoal>(jdbcTemplate, mapper) {
    override fun getSqlQuery(): StringBuilder = StringBuilder("SELECT * FROM save_goals WHERE 1=1")
    override fun getSqlCountQuery(): StringBuilder = StringBuilder("SELECT COUNT(*) FROM save_goals WHERE 1=1")

    override fun getSqlStringBuilder(
        sqlBuilder: StringBuilder,
        queryFilter: QueryFilter,
        query: IQueryExtend<SavingGoal>
    ): SqlQueryBuilder {
        val extend = query as QuerySavingGoalExtend
        val params = MapSqlParameterSource()

        if (!extend.accountIds.isEmpty()) {
            sqlBuilder.append(" AND account_id IN (:accounts)")
            params.addValue("accounts", extend.accountIds)
        }

        return SqlQueryBuilder(sqlBuilder, params)
    }

    override fun getRawMapper(): RowMapper<JdbcSavingGoalModel> {
        return RowMapper { rs, _ ->
            JdbcSavingGoalModel(
                id = rs.getObject("save_goal_id", UUID::class.java),
                name = rs.getObject("title", String::class.java),
                target = rs.getDouble("target"),
                balance = rs.getDouble("balance"),
                desirValue = rs.getInt("desir_value"),
                importance = rs.getInt("importance"),
                wishDueDate = rs.getObject("wish_due_date", LocalDate::class.java),
                description = rs.getString("description"),
                accountId = rs.getObject("account_id", UUID::class.java)
            )
        }
    }
}
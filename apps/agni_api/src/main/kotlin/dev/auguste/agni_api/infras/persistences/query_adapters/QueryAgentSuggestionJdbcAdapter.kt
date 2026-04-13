package dev.auguste.agni_api.infras.persistences.query_adapters

import com.fasterxml.jackson.databind.ObjectMapper
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryAgentSuggestionExtend
import dev.auguste.agni_api.core.entities.AgentSuggestion
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.infras.persistences.IMapper
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcAgentSuggestionModel
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class QueryAgentSuggestionJdbcAdapter(
    jdbcTemplate: NamedParameterJdbcTemplate,
    mapper: IMapper<JdbcAgentSuggestionModel, AgentSuggestion>,
): BaseQueryExtendJdbcAdapter<JdbcAgentSuggestionModel, AgentSuggestion>(jdbcTemplate, mapper) {
    override fun getSqlStringBuilder(
        sqlBuilder: StringBuilder,
        queryFilter: QueryFilter,
        query: IQueryExtend<AgentSuggestion>
    ): SqlQueryBuilder {
        val extend = query as QueryAgentSuggestionExtend
        val params = MapSqlParameterSource()

        if (extend.status != null) {
            sqlBuilder.append(" AND status = :status")
            params.addValue("status", extend.status)
        }

        return SqlQueryBuilder(sqlBuilder, params)
    }

    override fun getSqlQuery(): StringBuilder = StringBuilder("SELECT * FROM agent_suggestions WHERE 1=1")

    override fun getSqlCountQuery(): StringBuilder = StringBuilder("SELECT COUNT(*) FROM agent_suggestions WHERE 1=1")

    override fun getRawMapper(): RowMapper<JdbcAgentSuggestionModel> {
        return RowMapper { rs, _ ->
            JdbcAgentSuggestionModel(
                id = rs.getObject("agent_suggestion_id", UUID::class.java),
                agentId = rs.getString("agent_id"),
                agentName = rs.getString("agent_name"),
                title = rs.getString("title"),
                description = rs.getString("description"),
                confidenceScore = rs.getDouble("confidence_score"),
                status = rs.getString("status")
            )
        }
    }
}
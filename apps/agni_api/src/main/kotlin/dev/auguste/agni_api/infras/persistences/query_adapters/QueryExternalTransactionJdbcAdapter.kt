package dev.auguste.agni_api.infras.persistences.query_adapters

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryExternalTransactionExtend
import dev.auguste.agni_api.core.entities.AgentSuggestion
import dev.auguste.agni_api.core.entities.ExternalTransaction
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.infras.persistences.IMapper
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcAgentSuggestionModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcExternalTransactionModel
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class QueryExternalTransactionJdbcAdapter(
    jdbcTemplate: NamedParameterJdbcTemplate,
    mapper: IMapper<JdbcExternalTransactionModel, ExternalTransaction>,
): BaseQueryExtendJdbcAdapter<JdbcExternalTransactionModel, ExternalTransaction>(jdbcTemplate, mapper) {
    override fun getSqlQuery(): StringBuilder = StringBuilder("SELECT * FROM external_transactions WHERE 1=1")

    override fun getSqlCountQuery(): StringBuilder = StringBuilder("SELECT COUNT(*) FROM external_transactions WHERE 1=1")

    override fun getSqlStringBuilder(
        sqlBuilder: StringBuilder,
        queryFilter: QueryFilter,
        query: IQueryExtend<ExternalTransaction>
    ): SqlQueryBuilder {
        val extend = query as QueryExternalTransactionExtend
        val params = MapSqlParameterSource()

        if (extend.isTreated != null) {
            sqlBuilder.append(" AND is_treated = :isTreated")
            params.addValue("isTreated", extend.isTreated)
        }

        return SqlQueryBuilder(sqlBuilder, params)
    }

    override fun getRawMapper(): RowMapper<JdbcExternalTransactionModel> {
        return RowMapper { rs, _ ->
            JdbcExternalTransactionModel(
                id = rs.getObject("external_transaction_id", UUID::class.java),
                accountId = rs.getString("account_id"),
                amount = rs.getDouble("amount"),
                merchantName = rs.getString("merchant_name"),
                categoryPrimary = rs.getString("category_primary"),
                categoryDetail = rs.getString("category_detail"),
                isTreated = rs.getBoolean("is_treated"),
                dateTransaction = rs.getObject("date_transaction", OffsetDateTime::class.java).toLocalDateTime()
            )
        }
    }
}
package dev.auguste.agni_api.infras.persistences.query_adapters

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryInvoiceExtend
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.infras.persistences.IMapper
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcInvoiceModel
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.sql.Types
import java.time.OffsetDateTime
import java.util.UUID

@Component
class QueryInvoiceExtendJdbcAdapter(
    jdbcTemplate: NamedParameterJdbcTemplate,
    mapper: IMapper<JdbcInvoiceModel, Invoice>
): BaseQueryExtendJdbcAdapter<JdbcInvoiceModel, Invoice>(jdbcTemplate, mapper) {
    override fun getSqlQuery(): StringBuilder = StringBuilder("SELECT * FROM transactions WHERE 1=1")
    override fun getSqlCountQuery(): StringBuilder = StringBuilder("SELECT COUNT(*) FROM transactions WHERE 1=1")

    override fun getSqlStringBuilder(
        sqlBuilder: StringBuilder,
        queryFilter: QueryFilter,
        query: IQueryExtend<Invoice>): SqlQueryBuilder {
        val extend = query as QueryInvoiceExtend
        val params = MapSqlParameterSource()

        if (!extend.accountIds.isNullOrEmpty()) {
            sqlBuilder.append(" AND account_id IN (:accounts)")
            params.addValue("accounts", extend.accountIds)
        }

        if (!extend.types.isNullOrEmpty()) {
            sqlBuilder.append(" AND LOWER(type) IN (:types)")
            params.addValue("types", extend.types.map { it.value.lowercase() }.toSet())
        }

        extend.status?.let {
            sqlBuilder.append(" AND LOWER(status) = :status")
            params.addValue("status", it.value.lowercase(), Types.VARCHAR)
        }

        extend.isFreeze?.let {
            sqlBuilder.append(" AND is_freeze = :isFreeze")
            params.addValue("isFreeze", it)
        }

        extend.mouvementType?.let {
            sqlBuilder.append(" AND LOWER(mouvement) = LOWER(:moment)")
            params.addValue("moment", it.value, Types.VARCHAR)
        }

        extend.endDate?.let {
            sqlBuilder.append(" AND date <= :endDate")
            params.addValue("endDate", it)
        }

        extend.startDate?.let {
            sqlBuilder.append(" AND date >= :startDate")
            params.addValue("startDate", it)
        }

        return SqlQueryBuilder(sqlBuilder, params)
    }

    override fun getRawMapper(): RowMapper<JdbcInvoiceModel> {
        return RowMapper { rs, _ ->
            JdbcInvoiceModel(
                id = rs.getObject("transaction_id", UUID::class.java),
                accountId = rs.getObject("account_id", UUID::class.java),
                isFreeze = rs.getBoolean("is_freeze"),
                status = rs.getString("status"),
                type = rs.getString("type"),
                mouvement = rs.getString("mouvement"),
                date = rs.getObject("date", OffsetDateTime::class.java).toLocalDateTime(),
                deductions = rs.getString("deductions"),
            )
        }
    }
}
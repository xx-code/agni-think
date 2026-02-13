package dev.auguste.agni_api.infras.persistences.readers

import com.fasterxml.jackson.databind.ObjectMapper
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.readers.IInvoicetransactionCountReader
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryInvoiceExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryTransactionExtend
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.Transaction
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.infras.persistences.IMapper
import dev.auguste.agni_api.infras.persistences.addPaginationSqlStringBuilder
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcInvoiceModel
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class JdbcInvoiceTransactionCountReader(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val objectMapper: ObjectMapper,
    private val mapper: IMapper<JdbcInvoiceModel, Invoice>
) : IInvoicetransactionCountReader {

    private fun buildStringSql(
        queryFilter: QueryFilter,
        queryInvoiceExtend: IQueryExtend<Invoice>,
        queryTransactionExtend: IQueryExtend<Transaction>,
        sql: StringBuilder,
        params: MapSqlParameterSource) : StringBuilder {

        val queryInvoiceExtend = queryInvoiceExtend as QueryInvoiceExtend
        val queryTransactionExtend = queryTransactionExtend as QueryTransactionExtend


        if (!queryInvoiceExtend.accountIds.isNullOrEmpty()) {
            sql.append(" AND t.account_id IN (:accounts)")
            params.addValue("accounts", queryInvoiceExtend.accountIds)
        }

        if (!queryInvoiceExtend.types.isNullOrEmpty()) {
            sql.append(" AND t.type IN (:types)")
            params.addValue("types", queryInvoiceExtend.types.map { it.value })
        }

        queryInvoiceExtend.status?.let {
            sql.append(" AND t.status = :status")
            params.addValue("status", it.value)
        }

        queryInvoiceExtend.isFreeze?.let {
            sql.append(" AND t.is_freeze = :isFreeze")
            params.addValue("isFreeze", it)
        }

        queryInvoiceExtend.startDate?.let {
            sql.append(" AND t.date >= :startDate")
            params.addValue("startDate", it)
        }

        queryInvoiceExtend.endDate?.let {
            sql.append(" AND t.date <= :endDate")
            params.addValue("endDate", it)
        }

        if (!queryTransactionExtend.categoryIds.isNullOrEmpty()) {
            sql.append(" AND r.category_id IN (:categories)")
            params.addValue("categories", queryTransactionExtend.categoryIds)
        }

        if (!queryTransactionExtend.tagIds.isNullOrEmpty()) {
            sql.append(" AND r.tag_ids @> CAST(:tags AS jsonb)")
            params.addValue("tags", objectMapper.writeValueAsString(queryTransactionExtend.tagIds))
        }

        if (!queryTransactionExtend.budgetIds.isNullOrEmpty()) {
            sql.append(" AND r.budget_ids @> CAST(:budgets AS jsonb)")
            params.addValue("budgets", objectMapper.writeValueAsString(queryTransactionExtend.budgetIds))
        }

        return addPaginationSqlStringBuilder(sql, params, queryFilter, mapper, true)
    }

    override fun count(
        queryInvoiceExtend: IQueryExtend<Invoice>,
        queryTransactionExtend: IQueryExtend<Transaction>): Long {
        var sql = StringBuilder("""
            SELECT COUNT(DISTINCT t.transaction_id) 
            FROM transactions t
            JOIN records r ON t.transaction_id = r.transaction_id
            WHERE 1=1
        """.trimIndent())
        val params = MapSqlParameterSource()

        sql = buildStringSql(QueryFilter(queryAll = true), queryInvoiceExtend, queryTransactionExtend, sql, params)

        return jdbcTemplate.queryForObject(sql.toString(), params, Long::class.java) ?: 0
    }

    override fun pagination(
        query: QueryFilter,
        queryInvoiceExtend: IQueryExtend<Invoice>,
        queryTransactionExtend: IQueryExtend<Transaction>
    ): ListOutput<Invoice> {
        val total = count(queryInvoiceExtend, queryTransactionExtend)

        var sql = StringBuilder("""
            SELECT DISTINCT ON (t.transaction_id) 
                t.transaction_id, 
                t.account_id,
                t.is_freeze,
                t.status,
                t.type,
                t.mouvement,
                t.date,
                t.deductions
            FROM transactions t
            JOIN records r ON t.transaction_id = r.transaction_id
            WHERE 1=1
        """.trimIndent())

        val params = MapSqlParameterSource()
        sql = buildStringSql(query, queryInvoiceExtend, queryTransactionExtend, sql, params)


        val row = RowMapper { rs, _ ->
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

        val results = jdbcTemplate.query(sql.toString(), params, row)

        return ListOutput(
            items = results.map { mapper.toDomain(it) },
            total = total
        )
    }
}

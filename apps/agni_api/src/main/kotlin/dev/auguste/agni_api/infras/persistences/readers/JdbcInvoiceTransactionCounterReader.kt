package dev.auguste.agni_api.infras.persistences.readers

import com.fasterxml.jackson.databind.ObjectMapper
import dev.auguste.agni_api.core.adapters.readers.IInvoicetransactionCountReader
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryInvoiceExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryTransactionExtend
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.Transaction
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
class JdbcInvoiceTransactionCountReader(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val objectMapper: ObjectMapper
) : IInvoicetransactionCountReader {

    override fun count(
        queryInvoiceExtend: IQueryExtend<Invoice>,
        queryTransactionExtend: IQueryExtend<Transaction>): Long {

        val queryInvoiceExtend = queryInvoiceExtend as QueryInvoiceExtend
        val queryTransactionExtend = queryTransactionExtend as QueryTransactionExtend

        val sql = StringBuilder("""
            SELECT COUNT(DISTINCT t.transaction_id) 
            FROM transactions t
            JOIN records r ON t.transaction_id = r.transaction_id
            WHERE 1=1
        """.trimIndent())

        val params = MapSqlParameterSource()

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

        return jdbcTemplate.queryForObject(sql.toString(), params, Long::class.java) ?: 0
    }

}

package persistences.readers

import adapters.dto.QueryFilter
import adapters.dto.QuerySortBy
import adapters.readers.IInvoiceTransactionReader
import adapters.repositories.IQueryExtendBuilder
import domain.entities.Invoice
import domain.entities.Transaction
import usecases.dto.ListOutput
import org.springframework.jdbc.core.DataClassRowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import persistences.IMapper
import persistences.JdbcQueryAdapter
import persistences.addPaginationSqlStringBuilder
import persistences.jbdc_model.JdbcInvoiceModel
import persistences.jbdc_model.JdbcTransactionModel


@Component
class JdbcInvoiceTransactionReader(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val mapperInvoice: IMapper<JdbcInvoiceModel, Invoice>,
    private val mapperTransaction: IMapper<JdbcTransactionModel, Transaction>,
    private val queryAdapter: JdbcQueryAdapter,
) : IInvoiceTransactionReader {

    private fun buildStringSql(
        queryFilter: QueryFilter,
        queryInvoiceExtend: IQueryExtendBuilder<Invoice>,
        queryTransactionExtend: IQueryExtendBuilder<Transaction>,
        sql: StringBuilder,
        params: MapSqlParameterSource,
        ensureOrderBy: Boolean = false
    ): StringBuilder {

        var reformSql = queryAdapter.getSqlStringBuilder(sql, queryInvoiceExtend, mapperInvoice, "i", params)
        reformSql = queryAdapter.getSqlStringBuilder(reformSql.sql, queryTransactionExtend, mapperTransaction, "t", params)

        // Un SELECT DISTINCT ON n'est valide que si l'ORDER BY commence par ses expressions :
        // un tri par defaut est applique quand la requete n'en demande aucun.
        val sortBy = if (queryFilter.sortBy.by in mapperInvoice.getSortField()) {
            queryFilter.sortBy
        } else {
            QuerySortBy(mapperInvoice.getEntityModelFieldName()["date"] ?: "date", false)
        }
        val effectiveFilter = if (ensureOrderBy) queryFilter.copy(sortBy = sortBy) else queryFilter

        return addPaginationSqlStringBuilder(reformSql.sql, params, effectiveFilter, mapperInvoice, true, "i")
    }


    override fun count(
        queryInvoiceExtend: IQueryExtendBuilder<Invoice>,
        queryTransactionExtend: IQueryExtendBuilder<Transaction>): Long {
        var sql = StringBuilder("""
            SELECT COUNT(DISTINCT i.${mapperInvoice.getEntityModelFieldName()["id"]}) 
            FROM ${mapperInvoice.getTableName()} i
            JOIN ${mapperTransaction.getTableName()} t ON t.${mapperTransaction.getEntityModelFieldName()["invoiceId"]} = i.${mapperInvoice.getEntityModelFieldName()["id"]}
            WHERE 1=1
        """.trimIndent())
        val params = MapSqlParameterSource()

        sql = buildStringSql(QueryFilter(queryAll = true), queryInvoiceExtend, queryTransactionExtend, sql, params)

        return jdbcTemplate.queryForObject(sql.toString(), params, Long::class.java) ?: 0
    }

    override fun filteredInvoiceIds(
        query: QueryFilter,
        queryInvoiceExtend: IQueryExtendBuilder<Invoice>,
        queryTransactionExtend: IQueryExtendBuilder<Transaction>
    ): ListOutput<Invoice> {
        val total = count(queryInvoiceExtend, queryTransactionExtend)

        var sql = StringBuilder("""
            SELECT DISTINCT ON (i.${mapperInvoice.getEntityModelFieldName()["id"]}) i.*
            FROM ${mapperInvoice.getTableName()} i
            JOIN ${mapperTransaction.getTableName()} t ON t.${mapperTransaction.getEntityModelFieldName()["invoiceId"]} = i.${mapperInvoice.getEntityModelFieldName()["id"]}
            WHERE 1=1
        """.trimIndent())

        val params = MapSqlParameterSource()
        sql = buildStringSql(query, queryInvoiceExtend, queryTransactionExtend, sql, params, true)

        val items = jdbcTemplate.query(
            sql.toString(),
            params,
            DataClassRowMapper(mapperInvoice.getModelClass())
        )

        return ListOutput(
            items.map { mapperInvoice.toDomain(it) },
            total
        )
    }
}

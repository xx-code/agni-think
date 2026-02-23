package dev.auguste.agni_api.infras.persistences.query_adapters

import com.fasterxml.jackson.databind.ObjectMapper
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryTransactionExtend
import dev.auguste.agni_api.core.entities.Transaction
import dev.auguste.agni_api.infras.persistences.IMapper
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcTransactionModel
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.UUID


@Component
class QueryTransactionExtendJdbcAdapter(
    jdbcTemplate: NamedParameterJdbcTemplate,
    mapper: IMapper<JdbcTransactionModel, Transaction>,
    private val objectMapper: ObjectMapper
): BaseQueryExtendJdbcAdapter<JdbcTransactionModel, Transaction>(jdbcTemplate, mapper) {
    override fun getSqlQuery(): StringBuilder = StringBuilder("SELECT * FROM records WHERE 1=1")
    override fun getSqlCountQuery(): StringBuilder = StringBuilder("SELECT COUNT(*) FROM records WHERE 1=1")

    override fun getSqlStringBuilder(
        sqlBuilder: StringBuilder,
        queryFilter: QueryFilter,
        query: IQueryExtend<Transaction>
    ): SqlQueryBuilder {
        val extend = query as QueryTransactionExtend
        val params = MapSqlParameterSource()

        if (!extend.budgetIds.isNullOrEmpty()) {
            sqlBuilder.append(" AND r.tag_ids ??| CAST(:tagIds AS text[])")
            params.addValue("budgetIds", extend.budgetIds.toTypedArray())
        }

        if (!extend.tagIds.isNullOrEmpty()) {
            sqlBuilder.append(" AND r.tag_ids ??| CAST(:tagIds AS text[])")
            params.addValue("tagIds", extend.tagIds.toTypedArray())
        }

        if (!extend.invoiceIds.isNullOrEmpty()) {
            sqlBuilder.append(" AND transaction_id IN (:invoiceIds)")
            params.addValue("invoiceIds", extend.invoiceIds)
        }

        if (!extend.categoryIds.isNullOrEmpty()) {
            sqlBuilder.append(" AND category_id IN (:categoryIds)")
            params.addValue("categoryIds", extend.categoryIds)
        }

        extend.maxAmount?.let {
            sqlBuilder.append(" AND money_amount <= :maxAmount")
            params.addValue("maxAmount", it)
        }

        extend.minAmount?.let {
            sqlBuilder.append(" AND money_amount >= :minAmount")
            params.addValue("minAmount", it)
        }

        return SqlQueryBuilder(sqlBuilder, params)
    }

    override fun getRawMapper(): RowMapper<JdbcTransactionModel> {
        return RowMapper { rs, _ ->
            JdbcTransactionModel(
                id = rs.getObject("record_id", UUID::class.java),
                invoiceId = rs.getObject("transaction_id" , UUID::class.java),
                amount = rs.getDouble("money_amount"),
                categoryId = rs.getObject("category_id", UUID::class.java),
                description = rs.getString("description"),
                tagIds = rs.getString("tag_ids")?.let {
                    objectMapper.readValue(it, Array<String>::class.java).map { id -> UUID.fromString(id) }.toSet()
                } ?: emptySet(),
                budgetIds = rs.getString("budget_ids")?.let {
                    objectMapper.readValue(it, Array<String>::class.java).map { id -> UUID.fromString(id) }.toSet()
                } ?: emptySet()
            )
        }
    }
}

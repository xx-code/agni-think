package dev.auguste.agni_api.infras.persistences.query_adapters

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryInternalLoanExtend
import dev.auguste.agni_api.core.entities.InternalLoan
import dev.auguste.agni_api.infras.persistences.IMapper
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcInternalLoanModal
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID

@Component
class QueryInternalLoanJdbcAdapter(
    jdbcTemplate: NamedParameterJdbcTemplate,
    mapper: IMapper<JdbcInternalLoanModal, InternalLoan>,
): BaseQueryExtendJdbcAdapter<JdbcInternalLoanModal, InternalLoan>(jdbcTemplate, mapper) {
    override fun getSqlQuery(): StringBuilder = StringBuilder("SELECT * FROM internal_loans WHERE 1=1")

    override fun getSqlCountQuery(): StringBuilder = StringBuilder("SELECT COUNT(*) FROM internal_loans WHERE 1=1")

    override fun getSqlStringBuilder(
        sqlBuilder: StringBuilder,
        queryFilter: QueryFilter,
        query: IQueryExtend<InternalLoan>
    ): SqlQueryBuilder {
        val extend = query as QueryInternalLoanExtend
        val params = MapSqlParameterSource()

        if (extend.invoiceId != null) {
            sqlBuilder.append(" AND invoice_id = :invoiceId")
            params.addValue("invoiceId", extend.invoiceId)
        }

        if (extend.fundSourceId != null) {
            sqlBuilder.append(" AND fund_source_id  = :fundSourceId")
            params.addValue("fundSourceId", extend.fundSourceId)
        }


        if (extend.creditCardId != null) {
            sqlBuilder.append(" AND credit_target_id = :creditTargetId")
            params.addValue("creditTargetId", extend.creditCardId)
        }

        return SqlQueryBuilder(sqlBuilder, params)
    }

    override fun getRawMapper(): RowMapper<JdbcInternalLoanModal> {
        return RowMapper { rs, _ ->
            JdbcInternalLoanModal(
                id = rs.getObject("internal_loan_id", UUID::class.java),
                creditTargetId = rs.getObject("credit_target_id", UUID::class.java),
                invoiceId = rs.getObject("invoice_id", UUID::class.java),
                fundSourceId = rs.getObject("fund_source_id", UUID::class.java),
                dueDate = rs.getObject("due_date", LocalDate::class.java),
            )
        }
    }

}
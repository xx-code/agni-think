package persistences.readers

import adapters.dto.FundSummaryOutput
import adapters.readers.IFundSummaryReader
import domain.enums.FundType
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
class JdbcFundSummaryReader(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
): IFundSummaryReader {
    override fun getSummary(type: FundType?): FundSummaryOutput {
        var sql = """
        SELECT 
            COALESCE(SUM(balance), 0) AS totalBalance, 
            COALESCE(SUM(target), 0)  AS totalTarget 
        FROM funds
        """.trimIndent()
        if (type != null)
            sql += " WHERE type = ${type.value}"

        return jdbcTemplate.queryForObject(sql, emptyMap<String, Any>()) { rs, _ ->
            FundSummaryOutput(
                totalTarget = rs.getLong("totalTarget"),
                totalBalance = rs.getLong("totalBalance")
            )
        } ?: FundSummaryOutput(totalTarget = 0, totalBalance = 0)
    }
}
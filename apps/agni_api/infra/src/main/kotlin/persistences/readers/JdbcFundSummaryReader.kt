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
        val sql = StringBuilder(
            """
        SELECT 
            COALESCE(SUM(balance), 0) AS totalBalance, 
            COALESCE(SUM(target), 0)  AS totalTarget 
        FROM funds
        """.trimIndent()
        )

        val params = mutableMapOf<String, Any>()

        if (type != null) {
            sql.append(" WHERE type = :type")
            params["type"] = type.value
        }

        val result = jdbcTemplate.queryForObject(sql.toString(), params) { rs, _ ->
            FundSummaryOutput(
                totalTarget = rs.getLong("totalTarget"),
                totalBalance = rs.getLong("totalBalance")
            )
        }

        return result ?: FundSummaryOutput(totalTarget = 0L, totalBalance = 0L)
    }
}
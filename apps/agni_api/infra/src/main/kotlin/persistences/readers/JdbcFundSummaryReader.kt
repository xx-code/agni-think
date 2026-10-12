package persistences.readers

import adapters.dto.FundSummaryOutput
import adapters.readers.IFundSummaryReader
import domain.enums.FundType
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class JdbcFundSummaryReader(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
): IFundSummaryReader {
    override fun getSummary(userId: UUID, type: FundType?): FundSummaryOutput {
        val sql = StringBuilder(
            """
        SELECT 
            COALESCE(SUM(balance), 0) AS totalBalance, 
            COALESCE(SUM(target), 0)  AS totalTarget 
        FROM funds WHERE user_id = :userId
        """.trimIndent()
        )

        val params = mutableMapOf<String, Any>()
        params["userId"] = userId

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
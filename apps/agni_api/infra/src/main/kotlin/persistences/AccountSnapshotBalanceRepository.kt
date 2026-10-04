package persistences

import adapters.dto.AccountSnapshotBalanceInput
import adapters.dto.AccountSnapshotBalanceOutput
import adapters.dto.QueryFilter
import adapters.repositories.IAccountBalanceSnapshotRepository
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import persistences.jbdc_model.JdbcAccountSnapshotBalance
import java.time.LocalDateTime
import java.util.UUID

@Component
class AccountSnapshotBalanceRepository(
    private val storage: AccountSnapshotStorage,
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : IAccountBalanceSnapshotRepository {

    override fun makeSnapshot(snapshot: AccountSnapshotBalanceInput) {
        storage.save(snapshot.toModel())
    }

    override fun makeManySnapshots(snapshots: List<AccountSnapshotBalanceInput>) {
        storage.saveAll(snapshots.map { it.toModel() })
    }

    override fun getSnapshotByAccountId(
        accountId: UUID,
        queryFilter: QueryFilter,
        startAt: LocalDateTime?,
        endAt: LocalDateTime?
    ): List<AccountSnapshotBalanceOutput> {
        val params = MapSqlParameterSource("accountId", accountId)

        val sql = StringBuilder("""
        SELECT account_snapshot_balance_id, account_id, balance, date
        FROM account_snapshot_balances
        WHERE account_id = :accountId
        """.trimIndent()
        )

        if (startAt != null) {
            sql.append(" AND date >= :startAt")
            params.addValue("startAt", startAt)
        }
        if (endAt != null) {
            sql.append(" AND date <= :endAt")
            params.addValue("endAt", endAt)
        }

        sql.append(" ORDER BY date DESC")

        if (!queryFilter.queryAll) {
            sql.append(" LIMIT :limit OFFSET :offset")
            params.addValue("limit", queryFilter.limit)
            params.addValue("offset", queryFilter.offset)
        }

        return jdbcTemplate.query(sql.toString(), params, rowMapper)
    }

    private val rowMapper = RowMapper { rs, _ ->
        AccountSnapshotBalanceOutput(
            id = rs.getObject("account_snapshot_balance_id", UUID::class.java),
            accountId = rs.getObject("account_id", UUID::class.java),
            balance = rs.getDouble("balance"),
            date = rs.getTimestamp("date").toLocalDateTime()
        )
    }

    private fun AccountSnapshotBalanceInput.toModel() = JdbcAccountSnapshotBalance(
        accountSnapshotBalanceId = UUID.randomUUID(),
        accountId = accountId,
        balance = balance,
        date = date
    )
}
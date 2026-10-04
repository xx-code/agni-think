package persistences.jbdc_model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("account_snapshot_balances")
data class JdbcAccountSnapshotBalance(
    @Id
    @get:JvmName("getIdentifier")
    val accountSnapshotBalanceId: UUID,
    val accountId: UUID,
    val balance: Double,
    val date: LocalDateTime,
): JdbcModel() {
    override fun getId(): UUID {
        return accountSnapshotBalanceId
    }
}
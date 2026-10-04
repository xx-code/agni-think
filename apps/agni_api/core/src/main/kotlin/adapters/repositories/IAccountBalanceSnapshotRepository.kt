package adapters.repositories

import adapters.dto.AccountSnapshotBalanceInput
import adapters.dto.AccountSnapshotBalanceOutput
import adapters.dto.QueryFilter
import java.time.LocalDateTime
import java.util.UUID

interface IAccountBalanceSnapshotRepository {
    fun makeSnapshot(snapshot: AccountSnapshotBalanceInput)
    fun makeManySnapshots(snapshots: List<AccountSnapshotBalanceInput>)
    fun getSnapshotByAccountId(accountId: UUID, queryFilter: QueryFilter, startAt: LocalDateTime? = null, endAt: LocalDateTime? = null): List<AccountSnapshotBalanceOutput>
}
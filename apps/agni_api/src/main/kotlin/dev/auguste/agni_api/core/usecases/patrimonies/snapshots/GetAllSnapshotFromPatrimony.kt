package dev.auguste.agni_api.core.usecases.patrimonies.snapshots

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryPatrimonySnapshotExtend
import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto.GetAllSnapshotPatrimonyInput
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto.GetSnapshotPatrimonyOutput

class GetAllSnapshotFromPatrimony(
    private val snapshotPatrimonyRepo: IRepository<PatrimonySnapshot>
): IUseCase<GetAllSnapshotPatrimonyInput, ListOutput<GetSnapshotPatrimonyOutput>> {

    override fun execAsync(input: GetAllSnapshotPatrimonyInput): ListOutput<GetSnapshotPatrimonyOutput> {
        val snapshots = snapshotPatrimonyRepo.getAll(input.query, QueryPatrimonySnapshotExtend(setOf(input.patrimonyId)))

        return ListOutput(
            items = snapshots.items.map {
                GetSnapshotPatrimonyOutput(
                    id = it.id,
                    patrimonyId = it.patrimonyId,
                    date = it.date,
                    status = it.status.value,
                    balance = it.currentBalanceObserved
                )
            },
            total = snapshots.total
        )
    }
}
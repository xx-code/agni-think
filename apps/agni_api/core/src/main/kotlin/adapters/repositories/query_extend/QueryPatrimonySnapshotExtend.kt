package adapters.repositories.query_extend

import adapters.repositories.IQueryExtend
import domain.entities.PatrimonySnapshot
import java.util.UUID

class QueryPatrimonySnapshotExtend(
    val patrimonyIds: Set<UUID>): IQueryExtend<domain.entities.PatrimonySnapshot> {

    override fun isStatisfy(entity: domain.entities.PatrimonySnapshot): Boolean {
        return patrimonyIds.contains(entity.patrimonyId)
    }
}
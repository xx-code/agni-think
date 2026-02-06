package dev.auguste.agni_api.core.adapters.repositories.query_extend

import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import java.util.UUID

class QuerySnapshotPatrimonyExtend(
    val patrimonyIds: Set<UUID>): IQueryExtend<PatrimonySnapshot> {

    override fun isStatisfy(entity: PatrimonySnapshot): Boolean {
        return patrimonyIds.contains(entity.patrimonyId)
    }
}
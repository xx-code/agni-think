package dev.auguste.agni_api.core.adapters.repositories

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.dto.RepoList
import dev.auguste.agni_api.core.entities.Entity
import java.util.UUID

interface IRepository<T: Entity> {
    fun create(entity: T)
    fun getAll(query: QueryFilter, queryExtend: IQueryExtend<T>? = null): RepoList<T>
    fun getManyByIds(ids: Set<UUID>): List<T>
    fun get(id: UUID): T?
    fun delete(id: UUID)
    fun deleteManyByIds(ids: Set<UUID>)
    fun update(entity: T)
    fun existsByName(name: String): Boolean
}
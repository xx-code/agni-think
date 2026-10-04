package adapters.repositories

import adapters.dto.QueryFilter
import adapters.dto.RepoList
import domain.entities.Entity
import java.util.UUID

interface IRepository<T: Entity> {
    fun create(entity: T)
    fun createMany(entities: List<T>)
    fun getAll(query: QueryFilter, queryExtend: IQueryExtendBuilder<T>? = null): RepoList<T>
    fun getManyByIds(ids: Set<UUID>): List<T>
    fun get(id: UUID): T?
    fun delete(id: UUID)
    fun deleteManyByIds(ids: Set<UUID>)
    fun deleteManyBy(queryExtend: IQueryExtendBuilder<T>)
    fun update(entity: T)
    fun existsByName(name: String): Boolean
    fun exist(queryExtend: IQueryExtendBuilder<T>): Boolean
}
package persistences

import adapters.repositories.ICacheStore
import adapters.repositories.IRepository
import domain.convertors.ICacheCodec
import domain.entities.Entity
import java.time.Duration
import java.util.UUID

class CachedRepository<TEntity : Entity>(
    private val delegate: IRepository<TEntity>,
    private val cache: ICacheStore,
    private val codec: ICacheCodec<TEntity>,
    private val name: String,
    private val ttl: Duration = Duration.ofMinutes(10),
) : IRepository<TEntity> by delegate {

    private fun key(id: UUID) = "app:v1:$name:$id"

    private fun fromCache(id: UUID): TEntity? {
        val map = cache.getMap(key(id)) ?: return null
        return try {
            codec.decode(map)
        } catch (e: Exception) {
            cache.evict(key(id))
            null
        }
    }

    override fun get(id: UUID): TEntity? =
        fromCache(id) ?: delegate.get(id)?.also { cache.putMap(key(id), codec.encode(it), ttl) }

    override fun getManyByIds(ids: Set<UUID>): List<TEntity> {
        val found = ids.mapNotNull(::fromCache)
        val missing = ids - found.map { it.id }.toSet()
        if (missing.isEmpty()) return found
        val loaded = delegate.getManyByIds(missing)
        loaded.forEach { cache.putMap(key(it.id), codec.encode(it), ttl) }
        return found + loaded
    }

    // update / delete / deleteManyByIds / deleteManyBy : inchangés (evictAfterCommit)
}
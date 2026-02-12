package dev.auguste.agni_api.infras.persistences

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.dto.RepoList
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.Entity
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcModel
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

abstract class JdbcRepository<TModel: JdbcModel, TEntity: Entity>(
    protected val storage: GenericStorage<TModel, UUID>,
    protected val modelMapper: IMapper<TModel, TEntity>,
    protected val queryExtendAdapter: IQueryExtendJdbcAdapter<TModel, TEntity>? = null,
): IRepository<TEntity> {

    override fun create(entity: TEntity) {
        val model = modelMapper.toModel(entity)
        storage.save(model)
    }

    override fun getAll(
        query: QueryFilter,
        queryExtend: IQueryExtend<TEntity>?
    ): RepoList<TEntity> {
        var sort = Sort.unsorted()
        if (modelMapper.getSortField().isNotEmpty()) {
            if (query.sortBy.by.isNotBlank() && modelMapper.getSortField().contains(query.sortBy.by)) {
                val direction = if (query.sortBy.ascending) Sort.Direction.ASC else Sort.Direction.DESC
                sort = Sort.by(direction, query.sortBy.by)
            }
        }


        var pageable = Pageable.unpaged()
        if (!query.queryAll) {
            val pageIndex = query.offset / query.limit
            pageable = PageRequest.of(pageIndex, query.limit, sort)
        }

        if (queryExtend != null) {
            if (queryExtendAdapter == null)
                throw Error("Query Adapter not setup")

            val results = queryExtendAdapter.filter(query, queryExtend)

            return RepoList(
                items = results.map { modelMapper.toDomain(it) },
                total = storage.count()
            )
        }

        val results = storage.findAll(pageable)
            .map(modelMapper::toDomain)
            .content

        return RepoList(
            items = results.toList(),
            total = storage.count()
        )
    }

    override fun getManyByIds(ids: Set<UUID>): List<TEntity> {
        val results = storage.findAllById(ids).map { modelMapper.toDomain(it)}.toList()

        return results
    }

    override fun get(id: UUID): TEntity? {
        val result = storage.findByIdOrNull(id)
        if (result != null) {
            return modelMapper.toDomain(result)
        }

        return null
    }

    override fun delete(id: UUID) {
        storage.deleteById(id)
    }

    override fun deleteManyByIds(ids: Set<UUID>) {
        storage.deleteAllById(ids)
    }

    override fun update(entity: TEntity) {
        val model = modelMapper.toModel(entity)
        model.setAsExisting()
        storage.save(model)
    }

    override fun existsByName(name: String): Boolean {
        return false
    }
}

@Component
class JdbcUnitOfWork(
    val transactionTemplate: TransactionTemplate,
): IUnitOfWork {
    override fun <T> execute(block: () -> T): T {
        return transactionTemplate.execute { block() } ?: throw IllegalStateException("Transaction yielded no result")
    }

}
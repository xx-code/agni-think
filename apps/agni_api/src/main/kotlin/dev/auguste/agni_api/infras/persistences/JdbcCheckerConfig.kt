package dev.auguste.agni_api.infras.persistences

import dev.auguste.agni_api.core.adapters.IChecker
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.QueryExtendBuilder
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryComparator
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.core.entities.Transaction
import org.springframework.stereotype.Component

@Component
class JdbcCategoryConfig(
    private val transactionRepo: IRepository<Transaction>
): IChecker<Category> {
    override fun isInUse(entity: Category): Boolean {
        val condition = QueryExtendBuilder<Transaction>()
            .addCondition("categoryId", QueryComparator.Equal, entity.id)
        val transactions = transactionRepo.getAll(QueryFilter.queryAll(), condition)

        return transactions.items.isNotEmpty()
    }
}

@Component
class JdbcTagConfig(
    private val transactionRepo: IRepository<Transaction>
): IChecker<Tag> {
    override fun isInUse(entity: Tag): Boolean {
        val condition = QueryExtendBuilder<Transaction>()
            .addCondition("tagIds", QueryComparator.In, entity.id)
        val transactions = transactionRepo.getAll(QueryFilter.queryAll(), condition)

        return transactions.items.isNotEmpty()
    }
}
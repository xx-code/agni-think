package dev.auguste.agni_api.infras.persistences

import adapters.IChecker
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.query_extend.QueryComparator
import domain.entities.Category
import domain.entities.Tag
import domain.entities.Transaction
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
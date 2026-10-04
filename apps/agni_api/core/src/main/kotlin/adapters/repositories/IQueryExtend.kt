package adapters.repositories

import adapters.repositories.QueryComparator
import domain.entities.Entity

interface IQueryCondition<V> {
    val fieldName: String
    val operator: QueryComparator
    val value: V
}

data class QueryCondition<V>(
    override val fieldName: String,
    override val operator: QueryComparator,
    override val value: V
): IQueryCondition<V>

interface IQueryExtendBuilder<T> {
    fun <V> addCondition(fieldName: String, operator: QueryComparator, value: V): IQueryExtendBuilder<T>
    fun addCondition(condition: IQueryCondition<T>): IQueryExtendBuilder<T>
    fun satisfy(entity: T): Boolean

    fun getConditions(): List<IQueryCondition<*>>
}
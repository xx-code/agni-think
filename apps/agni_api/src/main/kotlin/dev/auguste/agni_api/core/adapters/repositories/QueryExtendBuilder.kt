package dev.auguste.agni_api.core.adapters.repositories

import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryComparator

class QueryExtendBuilder<T>: IQueryExtendBuilder<T> {
    private val conditions = mutableListOf<IQueryCondition<*>>()

    override fun <V> addCondition(
        fieldName: String,
        operator: QueryComparator,
        value: V
    ): IQueryExtendBuilder<T> {
        conditions.add(QueryCondition(fieldName, operator, value))
        return this
    }

    override fun addCondition(condition: IQueryCondition<T>): IQueryExtendBuilder<T> {
        conditions.add(condition)
        return this
    }

    override fun getConditions(): List<IQueryCondition<*>> = conditions.toList()

    private fun extractFieldValue(target: Any?, fieldName: String): Any? {
        if (target == null) return null

        // Si le nom du champ ne contient pas de point, on extrait directement la propriété
        if (!fieldName.contains(".")) {
            return extractProperty(target, fieldName)
        }

        // Séparation du premier segment ("scheduler") et du reste ("date" ou "config.startDate")
        val parts = fieldName.split(".", limit = 2)
        val currentProperty = parts[0]
        val remainingPath = parts[1]

        val nextObject = extractProperty(target, currentProperty) ?: return null

        // Descend récursivement dans l'objet suivant avec la suite du chemin
        return extractFieldValue(nextObject, remainingPath)
    }

    private fun extractProperty(obj: Any, propertyName: String): Any? {
        // Recherche de la propriété membre (getter/val/var)
        val property = obj::class.members.find { it.name == propertyName }
        return try {
            property?.call(obj)
        } catch (e: Exception) {
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun evaluate(entityValue: Any?, comparator: QueryComparator, value: Any?): Boolean {
        if (entityValue == null && value == null) return true
        if (entityValue == null || value == null) return false

        if (entityValue is Comparable<*> && value is Comparable<*>) {
            val a = entityValue as Comparable<Any>
            val b = value as Comparable<Any>

            return when (comparator) {
                QueryComparator.Greater -> a > b
                QueryComparator.GreaterOrEquals -> a >= b
                QueryComparator.Lesser -> a < b
                QueryComparator.LesserOrEquals -> a <= b
                QueryComparator.Equal -> a == b
                QueryComparator.In -> (value as? Collection<*>)?.contains(entityValue) == true
            }
        }

        // Fallback pour les égalités simples et collections
        return when (comparator) {
            QueryComparator.Equal -> entityValue == value
            QueryComparator.In -> (value as? Collection<*>)?.contains(entityValue) == true
            else -> false
        }
    }

    override fun satisfy(entity: T): Boolean {
        return conditions.all {
            val entityValue = extractFieldValue(entity, it.fieldName)
            evaluate(entityValue, it.operator, it.value)
        }
    }


}
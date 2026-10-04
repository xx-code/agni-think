package adapters.repositories


class QueryExtendBuilder<T>: IQueryExtendBuilder<T> {
    private val conditions = mutableListOf<IQueryCondition<*>>()

    override fun <V> addCondition(
        fieldName: String,
        operator: QueryComparator,
        value: V
    ): IQueryExtendBuilder<T> {
        if (value == null)
            return this
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

        // Convert entityValue to a Sequence or Collection if it's an Array/Collection
        val entityAsIterable = when (entityValue) {
            is Collection<*> -> entityValue
            is Array<*> -> entityValue.toList()
            is IntArray -> entityValue.toList()
            is LongArray -> entityValue.toList()
            else -> null
        }

        val valueAsCollection = when (value) {
            is Collection<*> -> value
            is Array<*> -> value.toList()
            is IntArray -> value.toList()
            is LongArray -> value.toList()
            else -> null
        }

        return when (comparator) {
            QueryComparator.Equal -> {
                when {
                    // If entityValue is an arrayOfIds/collection, check if it contains `value`
                    entityAsIterable != null -> value in entityAsIterable
                    // Standard equality fallback
                    else -> entityValue == value
                }
            }

            QueryComparator.NotEqual -> {
                when {
                    entityAsIterable != null -> value !in entityAsIterable
                    else -> entityValue != value
                }
            }

            QueryComparator.In -> {
                when {
                    // Both entityValue and value are collections/arrays: check for intersection
                    entityAsIterable != null && valueAsCollection != null -> {
                        entityAsIterable.any { it in valueAsCollection }
                    }
                    // Only entityValue is a collection: check if any ID in entityValue equals value
                    entityAsIterable != null -> entityAsIterable.contains(value)
                    // Only value is a collection: standard IN check
                    valueAsCollection != null -> valueAsCollection.contains(entityValue)
                    else -> false
                }
            }

            QueryComparator.NotIn -> {
                when {
                    entityAsIterable != null && valueAsCollection != null -> {
                        entityAsIterable.none { it in valueAsCollection }
                    }
                    entityAsIterable != null -> !entityAsIterable.contains(value)
                    valueAsCollection != null -> !valueAsCollection.contains(entityValue)
                    else -> true
                }
            }

            // Numerical / Comparable comparisons for single elements
            QueryComparator.Greater,
            QueryComparator.GreaterOrEquals,
            QueryComparator.Lesser,
            QueryComparator.LesserOrEquals -> {
                if (entityValue is Comparable<*> && value is Comparable<*>) {
                    val a = entityValue as Comparable<Any>
                    val b = value as Comparable<Any>
                    when (comparator) {
                        QueryComparator.Greater -> a > b
                        QueryComparator.GreaterOrEquals -> a >= b
                        QueryComparator.Lesser -> a < b
                        QueryComparator.LesserOrEquals -> a <= b
                        else -> false
                    }
                } else false
            }
        }
    }

    override fun satisfy(entity: T): Boolean {
        return conditions.all {
            val entityValue = extractFieldValue(entity, it.fieldName)
            evaluate(entityValue, it.operator, it.value)
        }
    }


}
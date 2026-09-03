package dev.auguste.agni_api.infras.persistences.query_adapters

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtendBuilder
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryComparator
import dev.auguste.agni_api.core.entities.Entity
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.infras.persistences.IMapper
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcModel
import org.springframework.jdbc.core.DataClassRowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class JdbcQueryAdapter(
    protected val jdbcTemplate: NamedParameterJdbcTemplate,
) {

    private fun <M : JdbcModel, E : Entity> getSqlQuery(mapper: IMapper<M, E>): StringBuilder = StringBuilder("SELECT * FROM ${mapper.getTableName()} WHERE 1=1")

    private fun <M : JdbcModel, E : Entity> getSqlCountQuery(mapper: IMapper<M, E>): StringBuilder = StringBuilder("SELECT COUNT(*) FROM ${mapper.getTableName()} WHERE 1=1")

    private fun <M : JdbcModel, E : Entity> getSqlStringBuilder(
        sqlBuilder: StringBuilder,
        queryBuilder: IQueryExtendBuilder<E>,
        mapper: IMapper<M, E>
    ): SqlQueryBuilder {
        val params = MapSqlParameterSource()
        val predicates = mutableListOf<String>()

        val fieldNameMapper = mapper.getEntityModelFieldName()

        for (condition in queryBuilder.getConditions()) {
            val mappedColumn = fieldNameMapper[condition.fieldName] ?: continue
            val rawParamName = condition.fieldName.replace(".", "_")

            // Formatage de la valeur (gestion spécifique des dates/instants)
            val formattedValue = formatConditionValue(condition.value)

            // Détection si la colonne est un accès JSONB (ex: scheduler->>'due_date')
            val isJsonPath = mappedColumn.contains("->>") || mappedColumn.contains("->")

            // Préparation des vérifications JSONB null
            val jsonExistPredicate = if (isJsonPath) buildJsonbExistsPredicate(mappedColumn) else null

            val predicate = when (condition.operator) {
                QueryComparator.Greater -> {
                    params.addValue(rawParamName, formattedValue)
                    "${formatSqlColumn(mappedColumn, formattedValue)} > :$rawParamName"
                }

                QueryComparator.GreaterOrEquals -> {
                    params.addValue(rawParamName, formattedValue)
                    "${formatSqlColumn(mappedColumn, formattedValue)} >= :$rawParamName"
                }

                QueryComparator.Lesser -> {
                    params.addValue(rawParamName, formattedValue)
                    "${formatSqlColumn(mappedColumn, formattedValue)} < :$rawParamName"
                }

                QueryComparator.LesserOrEquals -> {
                    params.addValue(rawParamName, formattedValue)
                    "${formatSqlColumn(mappedColumn, formattedValue)} <= :$rawParamName"
                }

                QueryComparator.Equal -> {
                    if (formattedValue == null) {
                        "$mappedColumn IS NULL"
                    } else {
                        params.addValue(rawParamName, formattedValue)
                        "${formatSqlColumn(mappedColumn, formattedValue)} = :$rawParamName"
                    }
                }

                QueryComparator.In -> {
                    val collection = (condition.value as? Collection<*>)?.map { formatConditionValue(it) }
                    if (!collection.isNullOrEmpty()) {
                        params.addValue(rawParamName, collection)
                        "${formatSqlColumn(mappedColumn, collection.first())} IN (:$rawParamName)"
                    } else null
                }
            }

            if (predicate != null) {
                if (jsonExistPredicate != null) {
                    // Combine jsonb_exists avec la condition
                    predicates.add("($jsonExistPredicate AND $predicate)")
                } else {
                    predicates.add(predicate)
                }
            }
        }

        if (predicates.isNotEmpty()) {
            sqlBuilder.append(" AND ").append(predicates.joinToString(separator = " AND "))
        }

        return SqlQueryBuilder(sqlBuilder, params)
    }

    /**
     * Converts Kotlin/Java date types into SQL-compatible types
     * (ISO-8601 strings for PostgreSQL TIMESTAMPTZ/JSONB).
     */
    private fun formatConditionValue(value: Any?): Any? {
        return when (value) {
            is LocalDate -> value.atStartOfDay().atOffset(ZoneOffset.UTC).toString()
            is LocalDateTime -> value.atOffset(ZoneOffset.UTC).toString()
            is Instant -> value.atOffset(ZoneOffset.UTC).toString()
            is OffsetDateTime -> value.toInstant().atOffset(ZoneOffset.UTC).toString()
            else -> value
        }
    }

    /**
     *  If the target column is a JSON field extracted as text (->>),
     *  applies a dynamic cast when necessary.
     */
    private fun formatSqlColumn(mappedColumn: String, sampleValue: Any?): String {
        if (!mappedColumn.contains("->>")) return mappedColumn

        // Cast automatique selon la valeur passée
        return when (sampleValue) {
            is String -> {
                // Tente de vérifier si la string est une Date ISO
                if (sampleValue.matches(Regex("^\\d{4}-\\d{2}-\\d{2}.*"))) {
                    "($mappedColumn)::timestamptz"
                } else {
                    mappedColumn
                }
            }
            is Number -> "($mappedColumn)::numeric"
            is Boolean -> "($mappedColumn)::boolean"
            else -> mappedColumn
        }
    }

    /**
     * Generates a `jsonb_exists(column, 'key')` check to avoid filtering
     * on null or non-existent JSON keys
     */
    private fun buildJsonbExistsPredicate(mappedColumn: String): String? {
        val parts = mappedColumn.split("->>")
        if (parts.size != 2) return null

        val jsonColumn = parts[0].trim()
        val jsonField = parts[1].trim().replace("'", "")

        return "jsonb_exists($jsonColumn, '$jsonField')"
    }

    fun <M : JdbcModel, E : Entity> toSpecification(
        builder: IQueryExtendBuilder<E>,
        mapper: IMapper<M, E>,
        queryFilter: QueryFilter?
    ): ListOutput<M> {
        val builderCounter = getSqlStringBuilder(getSqlCountQuery(mapper), builder, mapper)
        val total = jdbcTemplate.queryForObject(builderCounter.sql.toString(), builderCounter.params, Long::class.java) ?: 0L

        val builderQuery = getSqlStringBuilder(getSqlQuery(mapper), builder, mapper)
        var sql = builderQuery.sql
        if (queryFilter != null) {
            sql = addPaginationSqlStringBuilder(builderQuery.sql, builderQuery.params, queryFilter, mapper)
        }

        val items = jdbcTemplate.query(
            sql.toString(),
            builderQuery.params,
            DataClassRowMapper(mapper.getModelClass())
        )

        return ListOutput(
            items = items,
            total = total
        )
    }
}
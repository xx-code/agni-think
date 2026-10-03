package dev.auguste.agni_api.infras.persistences

import adapters.dto.QueryFilter
import adapters.repositories.IQueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Entity
import usecases.ListOutput
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
import java.util.UUID

data class SqlQueryBuilder(val sql: StringBuilder, val params: MapSqlParameterSource)

/**
 * Prefixe mapping column json`/`jsonb` contains a tableau of scalars
 * (`["uuid", "uuid"]`). A utiliser dans `IMapper.getEntityModelFieldName()`.
 */
const val JSON_SCALAR_ARRAY_PREFIX = "jsonb_scalar_array:"

/**
 * Prefixe de mapping column `jsonb` contains a tableau of scalars
 * (`[{"module": "Housing"}]`).
 */
const val JSON_OBJECT_ARRAY_PREFIX = "jsonb_array:"

internal fun <M: JdbcModel, E> addPaginationSqlStringBuilder(
    sql: StringBuilder,
    params: MapSqlParameterSource,
    queryFilter: QueryFilter,
    mapper: IMapper<M, E>,
    isDistinctTransaction: Boolean? = null,
    alias: String? = null
): StringBuilder {


    val allowedSortFields = mapper.getSortField()

    if (queryFilter.sortBy.by in allowedSortFields) {
        val sortField = queryFilter.sortBy.by
        val direction = if (queryFilter.sortBy.ascending) "ASC" else "DESC"
        // En jointure, les deux tables exposent souvent la meme colonne (date) : sans alias,
        // Postgres leve "column reference date is ambiguous".
        val qualifiedSortField = if (alias != null) "$alias.$sortField" else sortField

        if (isDistinctTransaction != null && isDistinctTransaction) {
            // DISTINCT ON impose que l'ORDER BY commence par les memes expressions
            val idColumn = mapper.getEntityModelFieldName()["id"] ?: "transaction_id"
            val distinctKey = if (alias != null) "$alias.$idColumn" else "t.$idColumn"
            sql.append(" ORDER BY $distinctKey, $qualifiedSortField $direction")
        } else {
            sql.append(" ORDER BY $qualifiedSortField $direction")
        }

    }

    if (queryFilter.queryAll) return sql

    sql.append(" LIMIT :limit OFFSET :offset")
    params.addValue("limit", queryFilter.limit)
    params.addValue("offset", queryFilter.offset)

    return sql
}

@Component
class JdbcQueryAdapter(
    protected val jdbcTemplate: NamedParameterJdbcTemplate,
) {
    private fun <M : JdbcModel, E : Entity> getSqlQuery(mapper: IMapper<M, E>): StringBuilder = StringBuilder("SELECT * FROM ${mapper.getTableName()} WHERE 1=1")
    private fun <M : JdbcModel, E : Entity> getSqlCountQuery(mapper: IMapper<M, E>): StringBuilder = StringBuilder("SELECT COUNT(*) FROM ${mapper.getTableName()} WHERE 1=1")

    fun <M : JdbcModel, E : Entity> getSqlStringBuilder(
        sqlBuilder: StringBuilder,
        queryBuilder: IQueryExtendBuilder<E>,
        mapper: IMapper<M, E>,
        alias: String? = null,
        sharedParams: MapSqlParameterSource? = null
    ): SqlQueryBuilder {
        val params: MapSqlParameterSource = sharedParams ?: MapSqlParameterSource()
        val predicates = mutableListOf<String>()

        val fieldNameMapper = mapper.getEntityModelFieldName()

        for (condition in queryBuilder.getConditions()) {
            val mapping = fieldNameMapper[condition.fieldName] ?: continue

            val rawParamName = condition.fieldName.replace(".", "_")
            val rawValue = condition.value
            val formattedValue = formatConditionValue(rawValue)

            // Le prefixe de convention doit etre lu avant l'alias : l'alias s'intercale entre
            // le prefixe et la colonne, sinon `startsWith` ne reconnait plus le type de colonne.
            val isJsonScalarArray = mapping.startsWith(JSON_SCALAR_ARRAY_PREFIX)
            val isJsonArray = mapping.startsWith(JSON_OBJECT_ARRAY_PREFIX)
            val mappedColumn = withAlias(mapping, alias)
            val isJsonPath = !isJsonScalarArray && !isJsonArray &&
                    (mappedColumn.contains("->>") || mappedColumn.contains("->"))

            val predicate = when {
                isJsonScalarArray -> buildJsonScalarArrayPredicate(
                    condition.operator,
                    mappedColumn,
                    rawParamName,
                    params,
                    rawValue
                )

                isJsonArray -> {
                    params.addValue(rawParamName, formattedValue)
                    buildJsonArrayPredicate(condition.operator, mappedColumn, rawParamName, params, formattedValue)
                }

                else -> {
                    val jsonExistPredicate = if (isJsonPath) buildJsonbExistsPredicate(mappedColumn) else null
                    val basePredicate = buildStandardPredicate(condition.operator, mappedColumn, rawParamName, params, rawValue, formattedValue)

                    if (basePredicate != null && jsonExistPredicate != null) {
                        "($jsonExistPredicate AND $basePredicate)"
                    } else {
                        basePredicate
                    }
                }
            }

            if (predicate != null) {
                predicates.add(predicate)
            }
        }

        if (predicates.isNotEmpty()) {
            sqlBuilder.append(" AND ").append(predicates.joinToString(separator = " AND "))
        }

        return SqlQueryBuilder(sqlBuilder, params)
    }

    /**
     * Intercale l'alias entre le prefixe de convention et la colonne :
     * `jsonb_scalar_array:budget_ids` + alias `t` devient `jsonb_scalar_array:t.budget_ids`.
     */
    private fun withAlias(mapping: String, alias: String?): String {
        if (alias == null) return mapping

        return when {
            mapping.startsWith(JSON_SCALAR_ARRAY_PREFIX) ->
                JSON_SCALAR_ARRAY_PREFIX + "$alias." + mapping.removePrefix(JSON_SCALAR_ARRAY_PREFIX)

            mapping.startsWith(JSON_OBJECT_ARRAY_PREFIX) ->
                JSON_OBJECT_ARRAY_PREFIX + "$alias." + mapping.removePrefix(JSON_OBJECT_ARRAY_PREFIX)

            else -> "$alias.$mapping"
        }
    }

    /**
     * Filtre une colonne `json`/`jsonb` qui contient un **tableau de valeurs scalaires**
     * (`records.budget_ids` stocke `["uuid"]`).
     *
     * `col IN (...)` est invalide sur ces colonnes : `jsonb` n'a pas d'opérateur `=`
     * (`operator does not exist: jsonb = unknown`). L'appartenance passe par l'opérateur de
     * containment `@>` : `@>` avec un `jsonb` teste l'inclusion d'un élément, `@>` avec un
     * `jsonb[]` teste l'inclusion d'au moins un élément, ce qui donne le `IN` attendu.
     *
     * Les deux côtés sont castés en `jsonb` pour que le meme mapping fonctionne sur une colonne
     * `json` comme sur une colonne `jsonb`, et la valeur est liée sous forme de littéral JSON
     * (`"uuid"`) : c'est ce que `@>` compare element par element.
     */
    private fun buildJsonScalarArrayPredicate(
        operator: QueryComparator,
        rawMapping: String,
        rawParamName: String,
        params: MapSqlParameterSource,
        rawValue: Any?,
    ): String? {
        val jsonColumn = "(${rawMapping.removePrefix(JSON_SCALAR_ARRAY_PREFIX)})::jsonb"

        return when (operator) {
            QueryComparator.Equal, QueryComparator.NotEqual -> {
                val value = rawValue ?: return null
                val name = uniqueParamName(rawParamName, params)
                params.addValue(name, toJsonbLiteral(formatConditionValue(value)))

                val contains = "$jsonColumn @> CAST(:$name AS jsonb)"
                if (operator == QueryComparator.Equal) contains else "NOT ($contains)"
            }

            QueryComparator.In, QueryComparator.NotIn -> {
                val values = (rawValue as? Collection<*>)?.filter { it != null } ?: return null
                if (values.isEmpty()) return null

                val literals = values.mapIndexed { index, value ->
                    val name = "${rawParamName}_${index}_"
                    params.addValue(name, toJsonbLiteral(formatConditionValue(value)))
                    "CAST(:$name AS jsonb)"
                }

                val contains = "$jsonColumn @> ANY(ARRAY[${literals.joinToString(", ")}])"
                if (operator == QueryComparator.In) contains else "NOT ($contains)"
            }

            else -> null
        }
    }

    /**
     * Sérialise une valeur Java en littéral JSON, car `@>` compare des valeurs `jsonb` :
     * un UUID doit être lié sous forme de chaine JSON (`"3f2b..."`), pas de texte nu.
     */
    private fun toJsonbLiteral(value: Any?): String = when (value) {
        null -> "null"
        is Boolean, is Number -> value.toString()
        is Collection<*> -> value.joinToString(separator = ",", prefix = "[", postfix = "]") {
            toJsonbLiteral(it)
        }

        else -> jsonbQuote(value.toString())
    }

    private fun jsonbQuote(raw: String): String =
        "\"" + raw.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

    /**
     * Génère un prédicat d'inclusion JSONB avec l'opérateur @> pour un tableau JSONB
     */
    private fun buildJsonArrayPredicate(
        operator: QueryComparator,
        rawMapping: String,
        rawParamName: String,
        params: MapSqlParameterSource,
        formattedValue: Any?,
    ): String? {
        val parsed = parseJsonArrayMapping(rawMapping)
        if (parsed != null && formattedValue != null) {
            val (columnName, jsonKey) = parsed
            return when (operator) {
                QueryComparator.Equal -> {
                    params.addValue(rawParamName, formattedValue.toString())
                    "$columnName @> jsonb_build_array(jsonb_build_object('$jsonKey', :$rawParamName))"
                }
                else -> null
            }
        }
        return null
    }

    /**
     * Construit les prédicats de comparaison standards (=, >, <, IN, etc.)
     */
    private fun buildStandardPredicate(
        operator: QueryComparator,
        mappedColumn: String,
        rawParamName: String,
        params: MapSqlParameterSource,
        rawValue: Any?,
        formattedValue: Any?
    ): String? {
        // Un nom déjà utilisé ne peut pas être réutilisé, sinon la valeur enregistrée
        // précédemment serait écrasée et le SQL référencerait un paramètre ambigu.
        val rawName = uniqueParamName(rawParamName, params)

        return when (operator) {
            QueryComparator.Greater -> {
                params.addValue(rawName, formattedValue)
                "${formatSqlColumn(mappedColumn, rawValue)} > ${formatSqlPlaceholder(rawName, rawValue)}"
            }
            QueryComparator.GreaterOrEquals -> {
                params.addValue(rawName, formattedValue)
                "${formatSqlColumn(mappedColumn, rawValue)} >= ${formatSqlPlaceholder(rawName, rawValue)}"
            }
            QueryComparator.Lesser -> {
                params.addValue(rawName, formattedValue)
                "${formatSqlColumn(mappedColumn, rawValue)} < ${formatSqlPlaceholder(rawName, rawValue)}"
            }
            QueryComparator.LesserOrEquals -> {
                params.addValue(rawName, formattedValue)
                "${formatSqlColumn(mappedColumn, rawValue)} <= ${formatSqlPlaceholder(rawName, rawValue)}"
            }
            QueryComparator.Equal -> {
                if (formattedValue == null) {
                    "$mappedColumn IS NULL"
                } else {
                    params.addValue(rawName, formattedValue)
                    "${formatSqlColumn(mappedColumn, rawValue)} = ${formatSqlPlaceholder(rawName, rawValue)}"
                }
            }
            QueryComparator.NotEqual -> {
                if (formattedValue == null) {
                    "$mappedColumn IS NOT NULL"
                } else {
                    params.addValue(rawName, formattedValue)
                    "${formatSqlColumn(mappedColumn, rawValue)} != ${formatSqlPlaceholder(rawName, rawValue)}"
                }
            }
            QueryComparator.In -> {
                val collection = (rawValue as? Collection<*>)?.map { formatConditionValue(it) }
                if (!collection.isNullOrEmpty()) {
                    params.addValue(rawName, collection)
                    "${formatSqlColumn(mappedColumn, collection.first())} IN (:$rawName)"
                } else null
            }

            QueryComparator.NotIn -> {
                val collection = (rawValue as? Collection<*>)?.map { formatConditionValue(it) }
                if (!collection.isNullOrEmpty()) {
                    params.addValue(rawName, collection)
                    "${formatSqlColumn(mappedColumn, collection.first())} NOT IN (:$rawName)"
                } else null
            }
        }
    }

    /**
     * Converts date/time types into values the driver can bind as a real temporal type.
     *
     * They must NOT be stringified: a bound String is sent as `character varying`, and
     * Postgres has no `timestamptz <= character varying` operator. Everything is normalized to
     * UTC `OffsetDateTime` because mappers persist dates with `atOffset(ZoneOffset.UTC)`.
     */
    private fun formatConditionValue(value: Any?): Any? {
        return when (value) {
            is LocalDateTime -> value.atOffset(ZoneOffset.UTC)
            is LocalDate -> value.atStartOfDay().atOffset(ZoneOffset.UTC)
            is Instant -> value.atOffset(ZoneOffset.UTC)
            is OffsetDateTime -> value.withOffsetSameInstant(ZoneOffset.UTC)
            is Enum<*> -> value.toString()
            else -> value
        }
    }

    private fun isTemporalValue(value: Any?): Boolean = value is LocalDate ||
            value is LocalDateTime ||
            value is Instant ||
            value is OffsetDateTime

    /**
     * Suffixe un nom de paramètre jusqu'à ce qu'il soit libre, pour que deux conditions portant sur
     * le même champ (ou deux tables d'une jointure) ne s'écrasent pas.
     */
    private fun uniqueParamName(rawParamName: String, params: MapSqlParameterSource): String {
        var suffix = 0
        var name = rawParamName
        while (params.hasValue(name)) {
            suffix++
            name = "${rawParamName}_${suffix}_"
        }
        return name
    }

    /**
     * Renders the bound parameter, casted to the type of the value it is compared with.
     *
     * `formatSqlColumn` casts JSON extractions (`->>` returns text) to the comparison type, so the
     * parameter has to be casted the same way for the operator to be resolvable. Binding a temporal
     * object already gives the right type; the cast also covers values arriving as a String.
     */
    private fun formatSqlPlaceholder(rawName: String, sampleValue: Any?): String =
        if (isTemporalValue(sampleValue)) ":$rawName::timestamptz" else ":$rawName"

    /**
     *  If the target column is a JSON field extracted as text (->>),
     *  applies a dynamic cast when necessary.
     */
    private fun formatSqlColumn(mappedColumn: String, sampleValue: Any?): String {
        val column = normalizeJsonPath(mappedColumn)

        return when {
            isTemporalValue(sampleValue) -> "($column)::timestamptz"

            column.contains("->>") -> {
                when (sampleValue) {
                    is UUID -> "($column)::uuid"
                    is Number -> "($column)::numeric"
                    is Boolean -> "($column)::boolean"
                    else -> column
                }
            }

            else -> column
        }
    }
    /**
     * Splits a JSON mapping such as `scheduler->>'repeater'->>'period'` into
     * `["scheduler", "repeater", "period"]`. Returns null for a plain column.
     */
    private fun splitJsonPath(mappedColumn: String): List<String>? {
        if (!mappedColumn.contains("->")) return null

        val parts = mappedColumn.split("->>'", "->'").map { it.trim() }
        return if (parts.size < 2 || parts.any { it.isEmpty() }) null else parts
    }
    /**
     * Only the last step of a JSON path can be an extraction: `->>` yields `text`,
     * and `text` has no `->>` operator (`operator does not exist: text ->> unknown`).
     * Intermediate steps must be traversals (`->`), which keep the `jsonb` type:
     * `scheduler->>'repeater'->>'period'` becomes `scheduler->'repeater'->>'period'`.
     */
    private fun normalizeJsonPath(mappedColumn: String): String {
        val parts = splitJsonPath(mappedColumn) ?: return mappedColumn
        if (parts.size < 3) return mappedColumn

        val keys = parts.drop(1).map { it.replace("'", "") }
        val traversals = keys.dropLast(1).joinToString("") { "->'$it'" }

        return "${parts.first()}$traversals->>'${keys.last()}'"
    }
    /**
     * Generates a `jsonb_exists(column, 'key')` check to avoid filtering
     * on null or non-existent JSON keys. For a nested path, the first key of
     * the path is the one to check: a missing parent already yields `NULL`,
     * which is filtered out by the comparison itself.
     */
    private fun buildJsonbExistsPredicate(mappedColumn: String): String? {
        val parts = splitJsonPath(mappedColumn) ?: return null

        val jsonColumn = parts[0].trim()
        val jsonField = parts[1].trim().replace("'", "")

        return "jsonb_exists($jsonColumn, '$jsonField')"
    }

    private fun parseJsonArrayMapping(mappedColumn: String): Pair<String, String>? {
        // Reçoit par ex: "jsonb_array:invoice_module_linkers->>'source_id'"
        val clean = mappedColumn.removePrefix(JSON_OBJECT_ARRAY_PREFIX)
        val parts = splitJsonPath(clean) ?: return null
        if (parts.size != 2) return null

        val columnName = parts[0].trim()
        val jsonKey = parts[1].trim().replace("'", "")

        return Pair(columnName, jsonKey)
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
package dev.auguste.agni_api.infras.persistences

import com.fasterxml.jackson.databind.ObjectMapper
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.ComparatorType
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryBudgetExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryInvoiceExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryPatrimonySnapshotExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QuerySavingGoalExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryScheduleInvoiceExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryTransactionExtend
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.Transaction
import dev.auguste.agni_api.core.value_objects.Scheduler
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcBudgetModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcInvoiceModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcPatrimonySnapshotModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcSavingGoalModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcScheduleInvoiceModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcTransactionModel
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.sql.Types
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.String
import kotlin.collections.toSet

// TODO: Refactoring

private fun <M, E>addPaginationSqlStringBuilder(
    sql: StringBuilder,
    params: MapSqlParameterSource,
    queryFilter: QueryFilter,
    mapper: IMapper<M, E>): StringBuilder {

    if (queryFilter.queryAll)
        return sql

    val allowedSortFields = mapper.getSortField()
    if (queryFilter.sortBy.by in allowedSortFields) {
        val sortField = queryFilter.sortBy.by
        val direction = if (queryFilter.sortBy.ascending) "ASC" else "DESC"
        sql.append(" ORDER BY $sortField $direction")
    }


    sql.append(" LIMIT :limit OFFSET :offset")
    params.addValue("limit", queryFilter.limit)
    params.addValue("offset", queryFilter.offset)

    return sql
}

@Component
class QueryInvoiceExtendJdbcAdapter(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val mapper: IMapper<JdbcInvoiceModel, Invoice>
): IQueryExtendJdbcAdapter<JdbcInvoiceModel, Invoice> {

    override fun getSqlStringBuilder(queryFilter: QueryFilter, query: IQueryExtend<Invoice>): SqlQueryBuilder {
        val extend = query as QueryInvoiceExtend
        val sql = StringBuilder("SELECT * FROM transactions WHERE 1=1")
        val params = MapSqlParameterSource()

        if (!extend.accountIds.isNullOrEmpty()) {
            sql.append(" AND account_id IN (:accounts)")
            params.addValue("accounts", extend.accountIds)
        }

        if (!extend.types.isNullOrEmpty()) {
            sql.append(" AND LOWER(type) IN (:types)")
            params.addValue("types", extend.types.map { it.value.lowercase() }.toSet())
        }

        extend.status?.let {
            sql.append(" AND LOWER(status) = LOWER(:status)")
            params.addValue("status", it.value, Types.VARCHAR)
        }

        extend.isFreeze?.let {
            sql.append(" AND is_freeze = :isFreeze")
            params.addValue("isFreeze", it)
        }

        extend.mouvementType?.let {
            sql.append(" AND LOWER(mouvement) = LOWER(:moment)")
            params.addValue("moment", it.value, Types.VARCHAR)
        }

        extend.endDate?.let {
            sql.append(" AND date <= :endDate")
            params.addValue("endDate", it)
        }

        extend.startDate?.let {
            sql.append(" AND date >= :startDate")
            params.addValue("startDate", it)
        }

        return SqlQueryBuilder(sql, params)
    }

    override fun getRawMapper(): RowMapper<JdbcInvoiceModel> {
        return RowMapper { rs, _ ->
            JdbcInvoiceModel(
                id = rs.getObject("transaction_id", UUID::class.java),
                accountId = rs.getObject("account_id", UUID::class.java),
                isFreeze = rs.getBoolean("is_freeze"),
                status = rs.getString("status"),
                type = rs.getString("type"),
                mouvement = rs.getString("mouvement"),
                date = rs.getObject("date", OffsetDateTime::class.java).toLocalDateTime(),
                deductions = rs.getString("deductions"),
            )
        }
    }

    override fun filter(queryFilter: QueryFilter, extend: IQueryExtend<Invoice>): List<JdbcInvoiceModel> {
        val builder = getSqlStringBuilder(queryFilter, extend)
        val sql = addPaginationSqlStringBuilder(builder.sql, builder.params, queryFilter, mapper)
        
        val rowMapper = getRawMapper()

        return jdbcTemplate.query(sql.toString(), builder.params, rowMapper)
    }
}

@Component
class QuerySavingGoalExtendJdbcAdapter(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val mapper: IMapper<JdbcSavingGoalModel, SavingGoal>
): IQueryExtendJdbcAdapter<JdbcSavingGoalModel, SavingGoal> {

    override fun getSqlStringBuilder(
        queryFilter: QueryFilter,
        query: IQueryExtend<SavingGoal>
    ): SqlQueryBuilder {
        val extend = query as QuerySavingGoalExtend
        val sql = StringBuilder("SELECT * FROM save_goals WHERE 1=1")
        val params = MapSqlParameterSource()

        if (!extend.accountIds.isEmpty()) {
            sql.append(" AND account_id IN (:accounts)")
            params.addValue("accounts", extend.accountIds)
        }

        return SqlQueryBuilder(sql, params)
    }

    override fun getRawMapper(): RowMapper<JdbcSavingGoalModel> {
        return RowMapper { rs, _ ->
            JdbcSavingGoalModel(
                id = rs.getObject("save_goal_id", UUID::class.java),
                name = rs.getObject("title", String::class.java),
                target = rs.getDouble("target"),
                balance = rs.getDouble("balance"),
                desirValue = rs.getInt("desir_value"),
                importance = rs.getInt("importance"),
                wishDueDate = rs.getObject("wish_due_date", LocalDate::class.java),
                description = rs.getString("description"),
                accountId = rs.getObject("account_id", UUID::class.java)
            )
        }
    }

    override fun filter(
        queryFilter: QueryFilter,
        extend: IQueryExtend<SavingGoal>
    ): List<JdbcSavingGoalModel> {
        val builder = getSqlStringBuilder(queryFilter, extend)
        val sql = addPaginationSqlStringBuilder(builder.sql, builder.params, queryFilter, mapper)

        val rowMapper = getRawMapper()

        return jdbcTemplate.query(sql.toString(), builder.params, rowMapper)
    }
}


@Component
class QueryScheduleInvoiceExtendJdbcAdapter(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val mapper: IMapper<JdbcScheduleInvoiceModel, ScheduleInvoice>,
    private val objectMapper: ObjectMapper
): IQueryExtendJdbcAdapter<JdbcScheduleInvoiceModel, ScheduleInvoice> {
    override fun getSqlStringBuilder(
        queryFilter: QueryFilter,
        query: IQueryExtend<ScheduleInvoice>
    ): SqlQueryBuilder {
        val extend = query as QueryScheduleInvoiceExtend
        val sql = StringBuilder("SELECT * FROM schedule_transactions WHERE 1=1")
        val params = MapSqlParameterSource()

        sql.append(" AND jsonb_exists(scheduler, 'due_date')")
        val dateToVerify =extend.comparatorDueDate.date.atOffset(ZoneOffset.UTC).toString()
        when(extend.comparatorDueDate.comparator) {
            ComparatorType.Greater -> {
                sql.append(" AND (scheduler->>'due_date')::timestamptz > :dueDate::timestamptz")
                sql.append(" AND (scheduler->>'due_date')::timestamptz > :dueDate::timestamptz")
                params.addValue("dueDate", dateToVerify)
            }
            ComparatorType.GreaterOrEquals -> {
                sql.append(" AND (scheduler->>'due_date')::timestampz >= :dueDate::timestamptz")
                params.addValue("dueDate", dateToVerify)
            }
            ComparatorType.Lesser -> {
                sql.append(" AND (scheduler->>'due_date')::timestampz < :dueDate::timestamptz")
                params.addValue("dueDate", dateToVerify)
            }
            ComparatorType.LesserOrEquals -> {
                sql.append(" AND (scheduler->>'due_date')::timestampz <= :dueDate::timestamptz")
                params.addValue("dueDate", dateToVerify)
            }
            ComparatorType.Equal -> {
                sql.append(" AND (scheduler->>'due_date')::timestampz = :dueDate::timestamptz")
                params.addValue("dueDate", dateToVerify)
            }
        }

        return SqlQueryBuilder(sql, params)
    }

    override fun getRawMapper(): RowMapper<JdbcScheduleInvoiceModel> {
        return  RowMapper { rs, _ ->
            JdbcScheduleInvoiceModel(
                id = rs.getObject("transaction_id", UUID::class.java),
                accountId = rs.getObject("account_id", UUID::class.java),
                categoryId = rs.getObject("category_id", UUID::class.java),
                amount = rs.getDouble("amount"),
                name = rs.getString("name"),
                type = rs.getString("type"),
                isPause = rs.getBoolean("is_pause"),
                isFreeze = rs.getBoolean("is_freeze"),
                scheduler = rs.getString("scheduler"),
                tagIds = rs.getString("tag_ids")?.let {
                    objectMapper.readValue(it, Array<String>::class.java).map { id -> UUID.fromString(id) }.toSet()
                } ?: emptySet()
            )
        }
    }

    override fun filter(
        queryFilter: QueryFilter,
        extend: IQueryExtend<ScheduleInvoice>
    ): List<JdbcScheduleInvoiceModel> {
        val builder = getSqlStringBuilder(queryFilter, extend)
        val sql = addPaginationSqlStringBuilder(builder.sql, builder.params, queryFilter, mapper)
        
        val rowMapper = getRawMapper()

        return jdbcTemplate.query(sql.toString(), builder.params, rowMapper)
    }
}

@Component
class QueryPatrimonySnapshotExtendJdbcAdapter(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val mapper: IMapper<JdbcPatrimonySnapshotModel, PatrimonySnapshot>,
): IQueryExtendJdbcAdapter<JdbcPatrimonySnapshotModel, PatrimonySnapshot> {
    override fun getSqlStringBuilder(
        queryFilter: QueryFilter,
        query: IQueryExtend<PatrimonySnapshot>
    ): SqlQueryBuilder {
        val extend = query as QueryPatrimonySnapshotExtend
        val sql = StringBuilder("SELECT * FROM patrimony_snapshots WHERE 1=1")
        val params = MapSqlParameterSource()

        if (!extend.patrimonyIds.isNotEmpty()) {
            sql.append(" AND patrimony_id IN :patrimonies")
            params.addValue("patrimonies", extend.patrimonyIds)
        }

        return SqlQueryBuilder(sql, params)
    }

    override fun getRawMapper(): RowMapper<JdbcPatrimonySnapshotModel> {
        return RowMapper { rs, _ ->
            JdbcPatrimonySnapshotModel(
                id = rs.getObject("patrimony_snapshot_id", UUID::class.java),
                patrimonyId = rs.getObject("patrimony_id", UUID::class.java),
                balance = rs.getDouble("balance"),
                date = rs.getObject("date", LocalDate::class.java),
                status = rs.getString("status")
            )
        }
    }

    override fun filter(
        queryFilter: QueryFilter,
        extend: IQueryExtend<PatrimonySnapshot>
    ): List<JdbcPatrimonySnapshotModel> {
        val builder = getSqlStringBuilder(queryFilter, extend)
        val sql = addPaginationSqlStringBuilder(builder.sql, builder.params, queryFilter, mapper)

        val rowMapper = getRawMapper()

        return jdbcTemplate.query(sql.toString(), builder.params, rowMapper)
    }
}

@Component
class QueryTransactionExtendJdbcAdapter(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val mapper: IMapper<JdbcTransactionModel, Transaction>,
    private val objectMapper: ObjectMapper
): IQueryExtendJdbcAdapter<JdbcTransactionModel, Transaction> {
    override fun getSqlStringBuilder(
        queryFilter: QueryFilter,
        query: IQueryExtend<Transaction>
    ): SqlQueryBuilder {
        val extend = query as QueryTransactionExtend
        val sql = StringBuilder("SELECT * FROM records WHERE 1=1")
        val params = MapSqlParameterSource()

        if (!extend.budgetIds.isNullOrEmpty()) {

            sql.append(" AND budget_ids @> CAST(:budgetIds AS jsonb)")
            params.addValue("budgetIds", objectMapper.writeValueAsString(extend.budgetIds))
        }


        if (!extend.tagIds.isNullOrEmpty()) {
            sql.append(" AND r.tag_ids @> CAST(:tagIds AS jsonb)")
            params.addValue("tagIds", objectMapper.writeValueAsString(extend.tagIds))
        }

        if (!extend.invoiceIds.isNullOrEmpty()) {
            sql.append(" AND transaction_id IN (:invoiceIds)")
            params.addValue("invoiceIds", extend.invoiceIds)
        }

        if (!extend.categoryIds.isNullOrEmpty()) {
            sql.append(" AND category_id IN (:categoryIds)")
            params.addValue("categoryIds", extend.categoryIds)
        }

        extend.maxAmount?.let {
            sql.append(" AND money_amount <= :maxAmount")
            params.addValue("maxAmount", it)
        }

        extend.minAmount?.let {
            sql.append(" AND money_amount >= :minAmount")
            params.addValue("minAmount", it)
        }

        return SqlQueryBuilder(sql, params)
    }

    override fun getRawMapper(): RowMapper<JdbcTransactionModel> {
        return RowMapper { rs, _ ->
            JdbcTransactionModel(
                id = rs.getObject("record_id", UUID::class.java),
                invoiceId = rs.getObject("transaction_id" , UUID::class.java),
                amount = rs.getDouble("money_amount"),
                categoryId = rs.getObject("category_id", UUID::class.java),
                description = rs.getString("description"),
                tagIds = rs.getString("tag_ids")?.let {
                    objectMapper.readValue(it, Array<String>::class.java).map { id -> UUID.fromString(id) }.toSet()
                } ?: emptySet(),
                budgetIds = rs.getString("budget_ids")?.let {
                    objectMapper.readValue(it, Array<String>::class.java).map { id -> UUID.fromString(id) }.toSet()
                } ?: emptySet()
            )
        }
    }

    override fun filter(
        queryFilter: QueryFilter,
        extend: IQueryExtend<Transaction>
    ): List<JdbcTransactionModel> {
        val builder = getSqlStringBuilder(queryFilter, extend)
        val sql = addPaginationSqlStringBuilder(builder.sql, builder.params, queryFilter, mapper)

        val rowMapper = getRawMapper()

        return jdbcTemplate.query(sql.toString(), builder.params, rowMapper)
    }
}

@Component
class QueryBudgetExtendJdbcAdapter(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val mapper: IMapper<JdbcPatrimonySnapshotModel, PatrimonySnapshot>,
    private val objectMapper: ObjectMapper
) : IQueryExtendJdbcAdapter<JdbcBudgetModel, Budget> {
    override fun getSqlStringBuilder(
        queryFilter: QueryFilter,
        query: IQueryExtend<Budget>,
    ): SqlQueryBuilder {
        val extend = query as QueryBudgetExtend
        val sql = StringBuilder("SELECT * FROM budgets WHERE 1=1")
        val params = MapSqlParameterSource()

        sql.append(" AND jsonb_exists(scheduler, 'due_date')")
        val dateToVerify =extend.scheduleDueDateComparator.date.atOffset(ZoneOffset.UTC).toString()
        when(extend.scheduleDueDateComparator.comparator) {
            ComparatorType.Greater -> {
                sql.append(" AND (scheduler->>'due_date')::timestamptz > :dueDate::timestamptz")
                params.addValue("dueDate", dateToVerify)
            }
            ComparatorType.GreaterOrEquals -> {
                sql.append(" AND (scheduler->>'due_date')::timestamptz >= :dueDate::timestamptz")
                params.addValue("dueDate", dateToVerify)
            }
            ComparatorType.Lesser -> {
                sql.append(" AND (scheduler->>'due_date')::timestamptz < :dueDate::timestamptz")
                params.addValue("dueDate", dateToVerify)
            }
            ComparatorType.LesserOrEquals -> {
                sql.append(" AND (scheduler->>'due_date')::timestamptz <= :dueDate::timestamptz")

                params.addValue("dueDate", dateToVerify)
            }
            ComparatorType.Equal -> {
                sql.append(" AND (scheduler->>'due_date')::timestamptz = :dueDate::timestamptz")
                params.addValue("dueDate", dateToVerify)
            }
        }

        return SqlQueryBuilder(sql, params)
    }

    override fun getRawMapper(): RowMapper<JdbcBudgetModel> {
        return RowMapper { rs, _ ->
            JdbcBudgetModel(
                id = rs.getObject("budget_id", UUID::class.java),
                name = rs.getString("title"),
                target = rs.getDouble("target"),
                scheduler = rs.getString("scheduler"),
                goalIds = rs.getString("save_goal_ids")?.let {
                    objectMapper.readValue(it, Array<String>::class.java).map { id -> UUID.fromString(id) }.toSet()
                } ?: emptySet(),
            )
        }
    }

    override fun filter(
        queryFilter: QueryFilter,
        extend: IQueryExtend<Budget>
    ): List<JdbcBudgetModel> {
        val builder = getSqlStringBuilder(queryFilter, extend)
        val sql = addPaginationSqlStringBuilder(builder.sql, builder.params, queryFilter, mapper)

        val rowMapper = getRawMapper()

        return jdbcTemplate.query(sql.toString(), builder.params, rowMapper)
    }

}

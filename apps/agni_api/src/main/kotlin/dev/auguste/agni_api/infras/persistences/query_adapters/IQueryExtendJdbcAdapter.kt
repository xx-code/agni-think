package dev.auguste.agni_api.infras.persistences.query_adapters

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.Entity
import dev.auguste.agni_api.core.usecases.ListOutput
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource

data class SqlQueryBuilder(val sql: StringBuilder, val params: MapSqlParameterSource)

interface IQueryExtendJdbcAdapter<TModel, TEntity: Entity> {
    fun getSqlQuery(): StringBuilder
    fun getSqlCountQuery(): StringBuilder
    fun getSqlStringBuilder(sqlBuilder: StringBuilder, queryFilter: QueryFilter, query: IQueryExtend<TEntity>): SqlQueryBuilder
    fun getRawMapper(): RowMapper<TModel>
    fun filter(queryFilter: QueryFilter, extend: IQueryExtend<TEntity>): ListOutput<TModel>
}
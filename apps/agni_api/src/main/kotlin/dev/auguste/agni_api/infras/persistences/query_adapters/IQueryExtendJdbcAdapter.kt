package dev.auguste.agni_api.infras.persistences.query_adapters

import adapters.dto.QueryFilter
import adapters.repositories.IQueryExtend
import domain.entities.Entity
import usecases.ListOutput
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
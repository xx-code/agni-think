package dev.auguste.agni_api.infras.persistences

import com.fasterxml.jackson.module.kotlin.readValue
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcModel
import tools.jackson.databind.ObjectMapper
import java.util.UUID

interface IMapper<TModel: JdbcModel, TEntity> {
    fun toDomain(model: TModel): TEntity
    fun toModel(entity: TEntity): TModel
    fun getEntityModelFieldName(): Map<String, String>
    fun getTableName(): String
    fun getSortField(): Set<String>
    fun getModelClass(): Class<TModel>

    companion object {

    }
}
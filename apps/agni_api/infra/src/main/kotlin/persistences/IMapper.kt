package persistences

import persistences.jbdc_model.JdbcModel

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
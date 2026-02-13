package dev.auguste.agni_api.infras.persistences

interface IMapper<TModel, TEntity> {
    fun toDomain(model: TModel): TEntity
    fun toModel(entity: TEntity): TModel
    fun getSortField(): Set<String>
}
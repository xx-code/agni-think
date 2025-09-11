import { QueryFilterExtend } from "@core/adapters/repository"
import Entity from "@core/domains/entities/entity"
import { Knex } from "knex"

export default interface Mapper<TEntity extends Entity, TModel> {
    toDomain(model: TModel): TEntity
    fromDomain(entity: TEntity): TModel
    getSortFilterFields(): string[]
    getIdField(): string
    getNameField(): string
}

export interface KnexFilterExtendAdapter<TEntity extends Entity, TModel>{
    filterQuery(query: Knex.QueryBuilder, filtersExtend: QueryFilterExtend<TEntity>): void
}

export interface KnexTable {
    getTableName(): string
    createTable(knex: Knex): Promise<void>
}
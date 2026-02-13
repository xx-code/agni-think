import Repository, { QueryFilterExtend } from "@core/adapters/repository";
import Entity from "@core/domains/entities/entity";
import { QueryFilterAllRepository, RepositoryListResult } from "@core/repositories/dto";
import { Knex } from "knex";
import Mapper, { KnexFilterExtendAdapter, KnexTable } from "./models/mapper";
import UnExpectedError from "@core/errors/unExpectedError";
import { KnexModel } from "./models/model";

export default class KnexRepository <TEntity extends Entity, TModel extends KnexModel> implements Repository<TEntity> {
    private connector: Knex
    private table: KnexTable
    private mapper: Mapper<TEntity, TModel>
    private filterAdapter?: KnexFilterExtendAdapter<TEntity, TModel>

    constructor(
        tableConnector: Knex, 
        table: KnexTable, 
        mapper: Mapper<TEntity, TModel>, filteAdapter?: KnexFilterExtendAdapter<TEntity, TModel>) {
        this.connector = tableConnector 
        this.mapper = mapper
        this.filterAdapter = filteAdapter
        this.table = table
    }

    async create(entity: TEntity, trx?: Knex.Transaction): Promise<void> {
        const model = this.mapper.fromDomain(entity) 

        if (trx === undefined)
            await this.connector(this.table.getTableName()).insert<TModel>(model)
        else 
            await trx(this.table.getTableName()).insert<TModel>(model)
    }

    async get(id: string, trx?: Knex.Transaction): Promise<TEntity | null> {
        let result 
        if (trx  === undefined)
            result = await this.connector(this.table.getTableName()).where(this.mapper.getIdField(), '=', id).select("*").first()
        else 
            result = await trx(this.table.getTableName()).where(this.mapper.getIdField(), '=', id).select("*").first()

        if (result)
            return this.mapper.toDomain(result as TModel)

        return null
    }

    async getAll(filters: QueryFilterAllRepository, filterExtend?: QueryFilterExtend<TEntity>, trx?: Knex.Transaction): Promise<RepositoryListResult<TEntity>> {
        let query
        if (trx == undefined)
            query = this.connector(this.table.getTableName()).select('*');
        else 
            query = trx(this.table.getTableName()).select('*');

        if (filters.sort) 
        {
            if (!this.mapper.getSortFilterFields().includes(filters.sort.sortBy))
                throw new UnExpectedError("SORT_FIELD_NOT_VALID")

            query.orderBy(filters.sort.sortBy, filters.sort.asc ? 'asc' : 'desc')
        }

        if (this.filterAdapter && filterExtend)
            this.filterAdapter.filterQuery(query, filterExtend)

        let total = await query.clone().clearSelect().clearOrder().count<{count: number}>("* as count").first() 
        const totalCount = total?.count ?? 0

        if (filters.queryAll === false)
            query.limit(filters.limit).offset(filters.offset)

        const results = await query;
        const entities: TEntity[] = []
        
        results.forEach((i: TModel) => {
            entities.push(this.mapper.toDomain(i))
        })

        return {
            items: entities,
            total: totalCount
        }
    }

    async getManyByIds(ids: string[], trx?: Knex.Transaction): Promise<TEntity[]> {
        let results = []
        if (trx === undefined)
            results = await this.connector(this.table.getTableName()).whereIn(this.mapper.getIdField(), ids).select('*')
        else 
            results = await trx(this.table.getTableName()).whereIn(this.mapper.getIdField(), ids).select('*')

        const entities: TEntity[] = []
        results.forEach((i: TModel) => {
            entities.push(this.mapper.toDomain(i))
        })

        return entities
    }

    async update(entity: TEntity, trx?: Knex.Transaction): Promise<void> {
        const model = this.mapper.fromDomain(entity) 
        if (trx === undefined)
            await this.connector(this.table.getTableName()).where(this.mapper.getIdField(), '=', entity.getId()).update(model)
        else 
            await trx(this.table.getTableName()).where(this.mapper.getIdField(), '=', entity.getId()).update(model)
    }

    async delete(id: string, trx?: Knex.Transaction): Promise<void> {
        if (trx === undefined)
            await this.connector(this.table.getTableName())
                .where(this.mapper.getIdField(), '=', id).delete()
        else
            await trx(this.table.getTableName())
                .where(this.mapper.getIdField(), '=', id).delete()
    }

    async existByName(name: string, trx?: Knex.Transaction): Promise<boolean> {
        let result
        if (trx === undefined)
            result = await this.connector(this.table.getTableName()).where(this.mapper.getNameField(), 'like' , name).first();
        else
            result = await trx(this.table.getTableName()).where(this.mapper.getNameField(), 'like' , name).first();

        return result !== undefined
    }
}
import { Record } from "@core/domains/entities/record";
import { RecordRepository } from "@core/repositories/recordRepository";
import knex, {Knex} from "knex";
import { KnexConnector } from "./postgreSqlConnector";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { mapperTransactionType } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import { QueryFilterAllRepository, RepositoryListResult } from "@core/repositories/dto";

export class PostgreSqlRecordRepository extends KnexConnector implements RecordRepository {
    
    constructor(connector: Knex) {
        super(connector);
    }
    
    async initialisation(): Promise<void> {
        let isExist = await this.connector.schema.hasTable('records') 
        if (!isExist) {
            await this.connector.schema.createTable('records', (table) => {
                table.uuid('record_id').primary()
                table.float('money_amount')
                table.date('date')
                table.string('type')
                table.string('description')
            })
        }
    }

    async save(request: Record): Promise<void> {
        await this.connector('records').insert({
            record_id: request.getId(),
            money_amount: request.getMoney().getAmount(),
            date: request.getUTCDate(),
            type: request.getType(),
            description: request.getDescription()
        })
    }
    async get(id: string): Promise<Record> {
        let result = await this.connector('records').where('record_id', id).select('*').first()

        if (!result)
            throw new ResourceNotFoundError(`Record with Id: ${id} not found`)

        return new Record(
            result['record_id'], 
            new Money(result['money_amount']), 
            result['date'], 
            mapperTransactionType(result['type']),
            result['description'])
    }
    async getAll(queryFilter: QueryFilterAllRepository): Promise<RepositoryListResult<Record>> {
        const query = this.connector('records').select('*')

        const total = await query.clone().clearSelect().clearOrder().count<{count: number}>("* as count").first() 
        const totalCount = total?.count ?? 0

        if (!queryFilter.queryAll)
            query.limit(queryFilter.limit).offset(queryFilter.offset)

        const results = await query;

        const records = results.map(result => (new Record(result['record_id'], 
            new Money(result['money_amount']), 
            result['date'], 
            mapperTransactionType(result['type']),
            result['description'])) )

        return { items: records, total: totalCount}
    }

    async getManyById(ids: string[]): Promise<Record[]> {
        let results = await this.connector('records').whereIn('record_id', ids).select('*')

        return results.map(result => (new Record(result['record_id'], 
            new Money(result['money_amount']), 
            result['date'], 
            mapperTransactionType(result['type']),
            result['description'])) )
    }
    
    async delete(id: string): Promise<void> {
        await this.connector('records').where('record_id', id).delete()    
    }

    async update(request: Record): Promise<void> {
        await this.connector('records').where('record_id', request.getId()).update({
            money_amount: request.getMoney().getAmount(),
            date: request.getUTCDate(),
            type: request.getType(),
            description: request.getDescription()
        })
    }
    
}
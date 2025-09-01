import { ScheduleTransactionRepository } from "@core/repositories/scheduleTransactionRepository";
import { KnexConnector } from "./postgreSqlConnector";
import { ScheduleTransaction } from "@core/domains/entities/scheduleTransaction";
import { Knex } from "knex";
import { isEmpty } from "@core/domains/helpers";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { Scheduler } from "@core/domains/valueObjects/scheduleInfo";
import { mapperMainTransactionCategory } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import { QueryAllFetch } from "@core/dto/base";
import { RepositoryListResult } from "@core/repositories/dto";

export class PostgreSqlScheduleTransactionRepository extends KnexConnector implements ScheduleTransactionRepository {
    constructor(connector: Knex) {
        super(connector)
    }
    
    initialisation(): Promise<void> {
        throw new Error("Method not implemented.");
    } 

    async existById(id: string): Promise<boolean> {
        let result = await this.connector('schedule_transactions').where('schedule_transaction_id', id).first();
        return !isEmpty(result);
    }

    async existByName(name: string): Promise<boolean> {
        let result = await this.connector('schedule_transactions').where('name', name).first();
        return !isEmpty(result);
    }

    async create(scheduleTransaction: ScheduleTransaction): Promise<void> {
        await this.connector('schedule_transactions').insert({
            schedule_transaction_id: scheduleTransaction.getId(),
            account_id: scheduleTransaction.getAccountRef(),
            category_id: scheduleTransaction.getCategoryRef(),
            amount: scheduleTransaction.getAmount().getAmount(),
            name: scheduleTransaction.getName(),
            type: scheduleTransaction.getTransactionType(),
            is_pause: scheduleTransaction.getIsPause(),
            is_pay: scheduleTransaction.getIsPay(),
            is_freeze: scheduleTransaction.getIsFreeze(),
            scheduler: scheduleTransaction.getSchedule().toJson()
        });

        if (scheduleTransaction.getTags().length > 0) {
            
            await this.connector('schedule_transaction_tags').insert(
                scheduleTransaction.getTags().map(tagId => ({
                    schedule_transaction_id: scheduleTransaction.getId(),
                    tag_id: tagId
                }))
            );
        }

    }

    async get(scheduleTransactionId: string): Promise<ScheduleTransaction | null> {
        let result = await this.connector('schedule_transactions')
            .where('schedule_transaction_id', scheduleTransactionId).select('*');

        if (result.length === 0) throw new ResourceNotFoundError(`Schudule transaction with ID ${scheduleTransactionId} not found`);

        const tagResults = await this.connector('schedule_transaction_tags').where('schedule_transaction_id', scheduleTransactionId).select('tag_id');
        const tags = tagResults.map(row => row.tag_id);

        return new ScheduleTransaction(
            result[0]['schedule_transaction_id'],
            result[0]['name'],
            result[0]['account_id'],
            result[0]['category_id'],
            new Money(result[0]['amount']),
            mapperMainTransactionCategory(result[0]['type']),
            Scheduler.fromJson(result[0]['scheduler']),
            result[0]['is_pay'],
            result[0]['is_pause'],
            result[0]['is_freeze'],
            tags
        );
    }
    async getAll(queryFilter: QueryAllFetch): Promise<RepositoryListResult<ScheduleTransaction>> {
        const query = this.connector('schedule_transactions').select('*'); 

        const total = await query.clone().clearSelect().clearOrder().count<{count: number}>("* as count").first() 
        const totalCount = total?.count ?? 0

        if (!queryFilter.queryAll)
            query.offset(queryFilter.offset).limit(queryFilter.limit)

        const results = await query;

        let transactions: ScheduleTransaction[] = []
        
        for(const result of results) {
            let tags = (await (this.connector('schedule_transaction_tags').where('schedule_transaction_id', result['schedule_transaction_id']).select('tag_id'))).map(result => result['tag_id'])
            let transaction = new ScheduleTransaction(
                result['schedule_transaction_id'],
                result['name'],
                result['account_id'],
                result['category_id'],
                new Money(result['amount']),
                mapperMainTransactionCategory(result['type']),
                Scheduler.fromJson(result['scheduler']),
                result['is_pay'],
                result['is_pause'],
                result['is_freeze'],
                tags
            );
            transactions.push(transaction);
        }


        return { items: transactions, total: totalCount};
    }

    async update(scheduleTransaction: ScheduleTransaction): Promise<void> {
        await this.connector('schedule_transactions')
        .where('schedule_transaction_id', scheduleTransaction.getId()).update({
            account_id: scheduleTransaction.getAccountRef(),
            category_id: scheduleTransaction.getCategoryRef(),
            amount: scheduleTransaction.getAmount().getAmount(),
            name: scheduleTransaction.getName(),
            type: scheduleTransaction.getTransactionType(),
            is_pause: scheduleTransaction.getIsPause(),
            is_pay: scheduleTransaction.getIsPay(),
            is_freeze: scheduleTransaction.getIsFreeze(),
            scheduler: scheduleTransaction.getSchedule().toJson()
        });


        await this.connector('schedule_transaction_tags').whereIn('tag_id', scheduleTransaction.getCollectionTags().__deleted_object.map(i => i.tagId)).delete();
        if (scheduleTransaction.getCollectionTags().__added_object.length > 0)
            await this.connector('schedule_transaction_tags').insert(scheduleTransaction.getCollectionTags().__added_object.map(el => ({schedule_transaction_id: scheduleTransaction.getId(), tag_id: el.tagId})))
    }

    async delete(scheduleTransactionId: string): Promise<void> {
        await this.connector('schedule_transactions').where('schedule_transaction_id', scheduleTransactionId).delete();
    }

}
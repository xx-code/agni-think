import { Record } from "@core/domains/entities/record";
import Mapper, { KnexFilterExtendAdapter, KnexTable } from "./mapper";
import { Knex } from "knex";
import { Money } from "@core/domains/entities/money";
import { KnexModel } from "./model";
import { RecordFilter } from "@core/adapters/repository";
import { mapperRecordType } from "@core/domains/constants";

export class KnexRecordTable implements KnexTable {
    getTableName(): string {
        return 'records'
    }
    async createTable(knex: Knex): Promise<void> {
         if (!(await knex.schema.hasTable('records')))
            await knex.schema.createTable('records', (table) => {
                table.uuid('record_id').primary()
                table.uuid('transaction_id').index()
                table.float('money_amount')
                table.uuid('category_id')
                table.string('description')
                table.jsonb('tag_ids')
                table.jsonb('budget_ids')
            });
    }
}

export async function createRecordTable(knex: Knex) {
    if (!(await knex.schema.hasTable('records')))
        await knex.schema.createTable('records', (table) => {
            table.uuid('record_id').primary()
            table.uuid('transaction_id').index()
            table.float('money_amount')
            table.uuid('category_id')
            table.string('description')
            table.jsonb('tag_ids')
            table.jsonb('budget_ids')
        });
}

export type RecordModel = KnexModel & {
    record_id: string
    transaction_id: string
    money_amount: number
    description: string
    category_id: any
    budget_ids: any
    tag_ids: any
}

export class RecordModelMapper implements Mapper<Record, RecordModel> {
    toDomain(model: RecordModel): Record {
        return new Record(
            model.record_id, 
            model.transaction_id,
            new Money(model.money_amount),  
            model.category_id,
            model.description,
            model.tag_ids ? Array.from(model.tag_ids) : [],
            model.budget_ids ? Array.from(model.budget_ids) : [],
        )
    }
    fromDomain(entity: Record): RecordModel {
        return {
            record_id: entity.getId(),
            transaction_id: entity.getTransactionId(),
            description: entity.getDescription(),
            category_id: entity.getCategoryRef(),
            money_amount: entity.getMoney().getAmount(),
            budget_ids: JSON.stringify(entity.getBudgetRefs()),
            tag_ids: JSON.stringify(entity.getTags()) 
        }
    }
    getSortFilterFields(): string[] {
        return ['money_amount']
    }
    getIdField(): string {
        return 'record_id'
    }
    getNameField(): string {
        return 'description'
    }

}

export class RecordFilterExtends implements KnexFilterExtendAdapter<Record, RecordModel> {
    filterQuery(query: Knex.QueryBuilder, filtersExtend: RecordFilter): void {
        if (filtersExtend.categories && filtersExtend.categories?.length > 0) query.whereIn('category_id', filtersExtend.categories);

        if (filtersExtend.tags && filtersExtend.tags?.length > 0)  {
            // query.whereIn('transaction_id', function() {
            //     this.select('transaction_id').from('transaction_tags').whereIn('tag_id', filtersExtend.tags!);
            // });
            // query.whereRaw('tag_ids ?| array[?]', filtersExtend.tags)
            query.whereRaw("tag_ids @> ?::jsonb", [JSON.stringify(filtersExtend.tags)]);
        }
        if (filtersExtend.budgets && filtersExtend.budgets?.length > 0) {
            // query.whereIn('transaction_id', function() {
            //     this.select('transaction_id').from('transaction_budgets').whereIn('budget_id', filtersExtend.budgets!);
            // });
            // query.whereJsonSubsetOf('budget_ids', JSON.stringify(filtersExtend.budgets))
            // query.whereRaw('budget_ids ?| array[?]',  filtersExtend.budgets)
            query.whereRaw("budget_ids @> ?::jsonb", [JSON.stringify(filtersExtend.budgets)]);
        }

        if (filtersExtend.transactionIds) {
            query.whereIn('transaction_id',  filtersExtend.transactionIds)
        } 
    }
}
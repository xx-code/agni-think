import { Transaction } from "@core/domains/entities/transaction"
import Mapper, { KnexFilterExtendAdapter, KnexTable } from "./mapper"
import { mapperMainTransactionCategory, mapperTransactionStatus } from "@core/domains/constants"
import { TransactionFilter } from "@core/adapters/repository"
import { Knex } from "knex"
import { KnexModel } from "./model"

export class KnexTransactionTable implements KnexTable {
    getTableName(): string {
        return 'transactions'
    }
    async createTable(knex: Knex): Promise<void> {
        if (!(await knex.schema.hasTable('transactions')))
            await knex.schema.createTable('transactions', (table) => {
                table.uuid('transaction_id').primary()
                table.uuid('account_id')
                table.uuid('record_id')
                table.uuid('category_id')
                table.string('status')
                table.string('type')
                table.date('date')
                table.boolean('is_freeze')
                table.jsonb('tag_ids')
                table.jsonb('budget_ids')
            });
    }
}

export type TransactionModel = KnexModel & {
    transaction_id: string 
    account_id: string
    record_id: string
    category_id: string
    status: string 
    type: string
    date: Date
    is_freeze: boolean
    budget_ids: any
    tag_ids: any
}

export class TransactionModelMapper implements Mapper<Transaction, TransactionModel> {
    toDomain(model: TransactionModel): Transaction {
        return new Transaction(
            model.transaction_id,
            model.account_id,
            model.record_id,
            model.category_id,
            model.date, 
            mapperMainTransactionCategory(model.type),
            mapperTransactionStatus(model.status),
            model.tag_ids ? Array.from(model.tag_ids) : [],
            model.budget_ids ? Array.from(model.budget_ids) : [],
            model.is_freeze
        )
    }
    fromDomain(entity: Transaction): TransactionModel {
        return {
            transaction_id: entity.getId(),
            account_id: entity.getAccountRef(),
            record_id: entity.getRecordRef(),
            category_id: entity.getCategoryRef(),
            status: entity.getStatus(),
            type: entity.getTransactionType(),
            date: entity.getUTCDate(),
            is_freeze: false,
            budget_ids: JSON.stringify(entity.getBudgetRefs()),
            tag_ids: JSON.stringify(entity.getTags()) 
        }
    }

    getIdField(): string {
        return 'transaction_id' 
    }
    
    getSortFilterFields(): string[] {
        return ['date']
    }

    getNameField(): string {
        throw new Error("Method not implemented.")
    }
}

export class TransactionFilterExtends implements KnexFilterExtendAdapter<Transaction, TransactionModel> {
    filterQuery(query: Knex.QueryBuilder, filtersExtend: TransactionFilter): void {
        if (filtersExtend.accounts && filtersExtend.accounts?.length > 0) query.whereIn('account_id', filtersExtend.accounts);

        if (filtersExtend.categories && filtersExtend.categories?.length > 0) query.whereIn('category_id', filtersExtend.categories);

        if ( filtersExtend.types && filtersExtend.types?.length > 0) query.whereIn('type', filtersExtend.types);

        if (filtersExtend.tags && filtersExtend.tags?.length > 0)  {
            // query.whereIn('transaction_id', function() {
            //     this.select('transaction_id').from('transaction_tags').whereIn('tag_id', filtersExtend.tags!);
            // });
            // query.whereRaw('tag_ids ?| array[?]', filtersExtend.tags)
            query.whereRaw("tag_ids @> ?::jsonb", JSON.stringify([filtersExtend.tags]));
        }
        if (filtersExtend.budgets && filtersExtend.budgets?.length > 0) {
            // query.whereIn('transaction_id', function() {
            //     this.select('transaction_id').from('transaction_budgets').whereIn('budget_id', filtersExtend.budgets!);
            // });
            // query.whereJsonSubsetOf('budget_ids', JSON.stringify(filtersExtend.budgets))
            // query.whereRaw('budget_ids ?| array[?]',  filtersExtend.budgets)
            query.whereRaw("budget_ids @> ?::jsonb", [JSON.stringify(filtersExtend.budgets)]);
        }

        if (filtersExtend.types && filtersExtend.types?.length > 0) query.whereIn('type', filtersExtend.types);

        if (filtersExtend.status)
            query.where('status', '=', filtersExtend.status)

        if (filtersExtend.isFreeze !== undefined)
            query.where('is_freeze', '=', filtersExtend.isFreeze);

        if (filtersExtend.startDate) 
            query.where('date', filtersExtend.strictStartDate ? '>' : '>=', filtersExtend.startDate);

        if (filtersExtend.endDate)  
            query.where('date', filtersExtend.strictEndDate ? '<' : '<=', filtersExtend.endDate);
    }
}
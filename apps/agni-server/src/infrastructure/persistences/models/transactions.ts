import { Transaction } from "@core/domains/entities/transaction"
import Mapper, { KnexFilterExtendAdapter, KnexTable } from "./mapper"
import { mapperMainTransactionCategory, mapperTransactionStatus } from "@core/domains/constants"
import { TransactionFilter } from "@core/adapters/repository"
import { Knex } from "knex"
import { KnexModel } from "./model"
import { TransactionDeduction } from "@core/domains/valueObjects/transactionDeduction"

export class KnexTransactionTable implements KnexTable {
    getTableName(): string {
        return 'transactions'
    }
    async createTable(knex: Knex): Promise<void> {
        if (!(await knex.schema.hasTable('transactions')))
            await knex.schema.createTable('transactions', (table) => {
                table.uuid('transaction_id').primary()
                table.uuid('account_id').index()
                table.string('status').index()
                table.string('type')
                table.date('date')
                table.boolean('is_freeze')
                table.jsonb('deductions')
            });
    }
}

export type TransactionModel = KnexModel & {
    transaction_id: string 
    account_id: string
    status: string 
    type: string
    date: Date
    deductions: any
    is_freeze: boolean
}

export class TransactionModelMapper implements Mapper<Transaction, TransactionModel> {
    toDomain(model: TransactionModel): Transaction {
        return new Transaction(
            model.transaction_id,
            model.account_id,
            model.date, 
            mapperMainTransactionCategory(model.type),
            mapperTransactionStatus(model.status),
            model.is_freeze,
            model.deductions ? Array.from(model.deductions).map(i => TransactionDeduction.fromJson(i)) : []
        )
    }
    fromDomain(entity: Transaction): TransactionModel {
        return {
            transaction_id: entity.getId(),
            account_id: entity.getAccountRef(),
            status: entity.getStatus(),
            type: entity.getTransactionType(),
            date: entity.getDate(),
            is_freeze: entity.getIsFreeze(),
            deductions: JSON.stringify(entity.getCollectionDeductions().map(i => i.toJson()))
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

        if ( filtersExtend.types && filtersExtend.types?.length > 0) query.whereIn('type', filtersExtend.types);

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
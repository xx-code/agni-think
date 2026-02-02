import { Transaction } from "@core/domains/entities/transaction"
import Mapper, { KnexFilterExtendAdapter, KnexTable } from "./mapper"
import { mapperMainTransactionCategory, mapperTransactionStatus } from "@core/domains/constants"
import { RecordFilter, TransactionFilter, TransactionRecordCountReader } from "@core/adapters/repository"
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
        const deductions = normalizeJsonbArray<any>(model.deductions)

        const transDeductions = deductions.map(d => {
            return TransactionDeduction.fromJson(d)
        })

        return new Transaction(
            model.transaction_id,
            model.account_id,
            model.date, 
            mapperMainTransactionCategory(model.type),
            mapperTransactionStatus(model.status),
            model.is_freeze,
            transDeductions
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

export class KnexTransactionRecordCountReader implements TransactionRecordCountReader {
    private knex: Knex

    constructor(knex: Knex) {
        this.knex = knex
    }

    async count(transaction: TransactionFilter, record: RecordFilter): Promise<number> {
        const query = 
                this.knex('transactions')
                    .join('records', 'transactions.transaction_id', '=', 'records.transaction_id')
                    .countDistinct<{count: number}>('transactions.transaction_id')
        
        
        if (transaction.accounts && transaction.accounts?.length > 0) query.whereIn('transactions.account_id', transaction.accounts);
        if (transaction.types && transaction.types?.length > 0) query.whereIn('transactions.type', transaction.types);
        if (transaction.types && transaction.types?.length > 0) query.whereIn('transactions.type', transaction.types);
        if (transaction.status)
            query.where('transactions.status', '=', transaction.status)
        if (transaction.isFreeze !== undefined)
            query.where('transactions.is_freeze', '=', transaction.isFreeze);
        if (transaction.startDate) 
            query.where('transactions.date', transaction.strictStartDate ? '>' : '>=', transaction.startDate);
        if (transaction.endDate)  
            query.where('transactions.date', transaction.strictEndDate ? '<' : '<=', transaction.endDate);

        if (record.categories && record.categories?.length > 0) query.whereIn('records.category_id', record.categories);

        if (record.tags && record.tags?.length > 0)  {
            query.whereRaw("records.tag_ids @> ?::jsonb", [JSON.stringify(record.tags)]);
        }
        if (record.budgets && record.budgets?.length > 0) {
            query.whereRaw("records.budget_ids @> ?::jsonb", [JSON.stringify(record.budgets)]);
        }

        const res = await query.first()

        const totalCount = res?.count ?? 0

        return totalCount 
    }

}

function normalizeJsonbArray<T>(value: unknown): T[] {
  if (!value) return []

  // JSONB came as string
  if (typeof value === 'string') {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed : [parsed]
  }

  // JSONB already parsed
  if (Array.isArray(value)) return value

  // Single object stored in JSONB
  if (typeof value === 'object') return [value as T]

  return []
}
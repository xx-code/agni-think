import { SortBy, TransactionFilter, TransactionRepository } from "@core/repositories/transactionRepository";
import { KnexConnector } from "./postgreSqlConnector";
import { Transaction } from "@core/domains/entities/transaction";
import { Knex } from "knex";
import { isEmpty } from "@core/domains/helpers";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { mapperMainTransactionCategory, mapperTransactionStatus } from "@core/domains/constants";
import { RepositoryListResult } from "@core/repositories/dto";

export class PostgreSqlTransactionRepository extends KnexConnector implements TransactionRepository {
    constructor(connector: Knex) {
        super(connector)
    }

    async initialisation(): Promise<void> {
        let isExist = await this.connector.schema.hasTable('transactions') 
        if (!isExist) {
            await this.connector.schema.createTable('transactions', (table) => {
                table.uuid('transaction_id').primary()
                table.uuid('account_id')
                table.uuid('record_id')
                table.uuid('category_id')
                table.string('status')
                table.string('type')
                table.date('date')
                table.boolean('is_freeze')
            });
        }
        
        isExist = await this.connector.schema.hasTable('transaction_tags') 
        if (!isExist) {
            await this.connector.schema.createTable('transaction_tags', (table) => {
                table.uuid('transaction_id').index().references('transaction_id').inTable('transactions').onDelete('CASCADE')
                table.uuid('tag_id').index().references('tag_id').inTable('tags').onDelete('CASCADE')
            })
        }
        isExist = await this.connector.schema.hasTable('transaction_budgets')
        if (!isExist){
            await this.connector.schema.createTable('transaction_budgets', (table) => {
                table.uuid('transaction_id').index().references('transaction_id').inTable('transactions').onDelete('CASCADE')
                table.uuid('budget_id').index().references('budget_id').inTable('budgets').onDelete('CASCADE')
            })
        }
    }

    async save(request: Transaction): Promise<void> {
        await this.connector('transactions').insert({
            transaction_id: request.getId(),
            account_id: request.getAccountRef(),
            record_id: request.getRecordRef(),
            category_id: request.getCategoryRef(),
            status: request.getStatus(),
            date: request.getDate(),
            type: request.getTransactionType(),
            is_freeze: request.getIsFreeze()
        });

        if (request.getTags().length > 0) {
            await this.connector('transaction_tags').insert(
                request.getTags().map(tagId => ({
                    transaction_id: request.getId(),
                    tag_id: tagId
                }))
            );
        }

        if (request.getBudgetRefs().length > 0) {
            await this.connector('transaction_budgets').insert(
                request.getBudgetRefs().map(budgetId => ({
                    transaction_id: request.getId(),
                    budget_id: budgetId
                }))
            )
        }
    }

    async get(id: string): Promise<Transaction> {
        let result = await this.connector('transactions').where('transaction_id', id).select('*');

        if (result.length === 0) throw new ResourceNotFoundError(`Transaction with ID ${id} not found`);

        let tagResults = await this.connector('transaction_tags').where('transaction_id', id).select('tag_id');
        let tags = tagResults.map(row => row.tag_id);

        let budgetResults = await this.connector('transaction_budgets').where('transaction_id', id).select('budget_id');
        let budgets = budgetResults.map(row => row.budget_id)

        return new Transaction(
            result[0]['transaction_id'],
            result[0]['account_id'],
            result[0]['record_id'],
            result[0]['category_id'],
            result[0]['date'],
            mapperMainTransactionCategory(result[0]['type']),
            mapperTransactionStatus(result[0]['status']),
            tags,
            budgets
        );
    }

    async isTransactionExistById(id: string): Promise<boolean> {
        let result = await this.connector('transactions').where('transaction_id', id).first();
        return !isEmpty(result)
    }

    async getPaginations(offset: number, size: number, sortBy: SortBy | null, filterBy: TransactionFilter): Promise<RepositoryListResult<Transaction>> { 
        try {
            let query = this.connector('transactions').select('*');

            if (sortBy) query.orderBy(sortBy.sortBy, sortBy.asc ? 'asc' : 'desc')

            if (filterBy.accounts.length > 0) query.whereIn('account_id', filterBy.accounts);
            if (filterBy.categories.length > 0) query.whereIn('category_id', filterBy.categories);
            if (filterBy.types.length > 0) query.whereIn('type', filterBy.types);
            if (filterBy.tags.length) {
                query.whereIn('transaction_id', function() {
                    this.select('transaction_id').from('transaction_tags').whereIn('tag_id', filterBy.tags);
                });
            }
            if (filterBy.budgets.length) {
                query.whereIn('transaction_id', function() {
                    this.select('transaction_id').from('transaction_budgets').whereIn('budget_id', filterBy.budgets);
                });
            }

            if (filterBy.startDate) 
                query.where('date', '>=', filterBy.startDate);
            if (filterBy.endDate) 
                query.where('date', '<=', filterBy.endDate);

        
            query.limit(size).offset(offset * size);

            let results = await query;

            let transactions: Transaction[] = []

            for (const result of results) {
                let tags = (await (this.connector('transaction_tags').where('transaction_id', result['transaction_id']).select('tag_id'))).map(result => result['tag_id'])
                let budgets = (await (this.connector('transaction_budgets').where('transaction_id', result['transaction_id']).select('budget_id'))).map(result => result['budget_id'])
                transactions.push(new Transaction(result['transaction_id'], result['account_id'], result['record_id'], 
                    result['category_id'], result['date'], result['type'], mapperTransactionStatus(result['status']), tags, budgets))
            }

            let total = await this.connector('transactions').count<{count: number}>('* as count').first()
            const totalCount = total?.count ?? 0

            return {
                items: transactions,
                total: totalCount
            };
        } catch(err) {
            console.log
            throw err
        } 
    }

    async getTransactions(filterBy: TransactionFilter): Promise<Transaction[]> {
        let query = this.connector('transactions').select('*');

        if (filterBy.accounts.length > 0) query.whereIn('account_id', filterBy.accounts);
            
        if (filterBy.categories.length > 0) query.whereIn('category_id', filterBy.categories);

        if (filterBy.types.length > 0) query.whereIn('type', filterBy.types);
        
        if (filterBy.tags.length > 0) {
            query.whereIn('transaction_id', function() {
                this.select('transaction_id').from('transaction_tags').whereIn('tag_id', filterBy.tags);
            });
        }
        if (filterBy.budgets.length) {
            query.whereIn('transaction_id', function() {
                this.select('transaction_id').from('transaction_budgets').whereIn('budget_id', filterBy.budgets);
            });
        }
        if (filterBy.startDate) 
            query.where('date', '>=', filterBy.startDate);

        if (filterBy.endDate)  
            query.where('date', '<=', filterBy.endDate);


        let results = await query;

        let transactions: Transaction[] = []
        
        for(let result of results) {
            let tags = (await (this.connector('transaction_tags').where('transaction_id', result['transaction_id']).select('tag_id'))).map(result => result['tag_id'])
            let budgets = (await (this.connector('transaction_budgets').where('transaction_id', result['transaction_id']).select('budget_id'))).map(result => result['budget_id'])
            let transaction = new Transaction(result['transaction_id'], result['account_id'], result['record_id'], 
                result['category_id'], result['date'], result['type'], mapperTransactionStatus(result['status']), tags, budgets)
            transactions.push(transaction)
        }

        return transactions
    }

    async delete(id: string): Promise<void> {
        await this.connector('transactions').where('transaction_id', id).delete();
    }

    async update(request: Transaction): Promise<void> {
        await this.connector('transactions').where('transaction_id', request.getId()).update({
            account_id: request.getAccountRef(),
            record_id: request.getRecordRef(),
            category_id: request.getCategoryRef(),
            status: request.getStatus(),
            type: request.getTransactionType(),
            is_freeze: request.getIsFreeze()
        });

        await this.connector('transaction_tags').whereIn('tag_id', request.getCollectionTags().__deleted_object.map(i => i.tagId)).delete();
        if (request.getCollectionTags().__added_object.length > 0)
            await this.connector('transaction_tags').insert(request.getCollectionTags().__added_object.map(el => ({transaction_id: request.getId(), tag_id: el.tagId})))

        await this.connector('transaction_budgets').whereIn('budget_id', request.getCollectionBudgets().__deleted_object.map(i => i.budgetId)).delete();
        if (request.getCollectionBudgets().__added_object.length > 0)
            await this.connector('transaction_budgets').insert(request.getCollectionBudgets().__added_object.map(el => ({transaction_id: request.getId(), budget_id: el.budgetId})))
    }

}
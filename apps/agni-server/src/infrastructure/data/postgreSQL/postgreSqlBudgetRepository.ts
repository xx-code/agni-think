import { BudgetRepository } from "@core/repositories/budgetRepository";
import { KnexConnector } from "./postgreSqlConnector";
import { Knex } from "knex";
import { Budget, BudgetBuilder } from "@core/domains/entities/budget";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { isEmpty } from "@core/domains/helpers";

export class PostgresSqlBudgetRepository extends KnexConnector implements BudgetRepository {
    constructor(connector: Knex) {
        super(connector)
    }

    async initialisation() {
        let isExist = await this.connector.schema.hasTable('budgets')
        if (!isExist) {
            await this.connector.schema.createTable('budgets', (table) => {
                table.uuid('budget_id').primary()
                table.string('title')
                table.float('target')
                table.date('date_start')
                table.date('date_update')
                table.date('date_end').nullable()
                table.string('period').nullable()
                table.integer('period_time').nullable()
                table.boolean('is_archived')
            })
        }
        
        isExist = await this.connector.schema.hasTable('budget_categories')
        if (!isExist) {
            await this.connector.schema.createTable('budget_categories', (table) => {
                table.uuid('budget_id').index().references('budget_id').inTable('budgets').onDelete('CASCADE')
                table.uuid('category_id').index().references('category_id').inTable('categories').onDelete('CASCADE')
            })
        }

        isExist = await this.connector.schema.hasTable('budget_tags')
        if (!isExist) {
            await this.connector.schema.createTable('budget_tags', (table) => {
                table.uuid('budget_id').index().references('budget_id').inTable('budgets').onDelete('CASCADE')
                table.uuid('tag_id').index().references('tag_id').inTable('tags').onDelete('CASCADE')
            })
        }
    }
    

    async isBudgetExistById(id: string): Promise<boolean> {
        const result = await this.connector('budgets').where('budget_id', id).first();
        return !isEmpty(result);
    }

    async isBudgetExistByName(title: string): Promise<boolean> {
        const result = await this.connector('budgets').where('title', 'like', `%${title}%`).select('budget_id').first();
        return !isEmpty(result)
    }

    async save(request: Budget): Promise<void> {
        let result = await this.connector('budgets').insert({
            budget_id: request.getId(),
            title: request.getTitle(),
            target: request.getTarget(),
            date_start: request.getDateStart(),
            date_update: request.getDateUpdate(),
            date_end: request.getDateEnd(),
            period: request.getPeriod(),
            period_time: request.getPeriodTime(),
            is_archived: request.getIsArchive(),
        });

        for (let category_id of request.getCategories()) 
            await this.connector('budget_categories').insert({
                budget_id: request.getId(),
                category_id: category_id
            })
        
        for (let tag_id of request.getTags())
            await this.connector('budget_tags').insert({
                budget_id: request.getId(),
                tag_id: tag_id
            })
    }

    async get(id: string): Promise<Budget> {
        const result = await this.connector('budgets').where('budget_id', id).first();
        if (!result) throw new ResourceNotFoundError(`Budget ${id} not found`);
        let budget = new Budget()

        let categories = await this.connector('budget_categories').where('budget_id', result['budget_id']).select('category_id')
        let tags = await this.connector('budget_tags').where('budget_id', result['budget_id']).select('tag_id')

        budget.initializer(
            result['budget_id'],
            result['is_archived'],
            result['target'],
            result['title'],
            result['date_start'],
            result['period'],
            result['period_time'],
            result['date_end'],
            result['date_update'],
            categories.map(cat => cat['category_id']),
            tags.map(tag => tag['tag_id'])
        )
        
        return budget
    }

    async getAll(): Promise<Budget[]> {
        const results = await this.connector('budgets');

        let budgets: Budget[] = []
        for(let result of results) {
            let categories = await this.connector('budget_categories').where('budget_id', result['budget_id']).select('category_id')
            let tags = await this.connector('budget_tags').where('budget_id', result['budget_id']).select('tag_id')


            let budget = new Budget()
            
            budget.initializer(
                result['budget_id'],
                result['is_archived'],
                result['target'],
                result['title'],
                result['date_start'],
                result['period'],
                result['period_time'],
                result['date_end'],
                result['date_update'],
                categories.map(cat => cat['category_id']),
                tags.map(tag => tag['tag_id'])
            )

            budgets.push(budget)
        } 

        return budgets
    }

    async delete(id: string): Promise<void> {
        await this.connector('budgets').where('budget_id', id).delete();
    }

    async toggleArchived(id: string, doArchive: boolean): Promise<void> {
        await this.connector('budgets').where('budget_id', id).update({ is_archived: doArchive });
    }

    async update(request: Budget): Promise<void> {
        await this.connector('budgets').where('budget_id', request.getId()).update({
            title: request.getTitle(),
            target: request.getTarget(),
            date_start: request.getDateStart(),
            date_update: request.getDateUpdate(),
            date_end: request.getDateEnd(),
            period: request.getPeriod(),
            period_time: request.getPeriodTime(),
            is_archived: request.getIsArchive(),
        });

        await this.connector('budget_categories').whereIn('category_id', request.__delete_event_category)
        await this.connector('budget_tags').whereIn('tag_id', request.__delete_event_tag)

        if (request.__add_event_category.length > 0)
            await this.connector('budget_categories').insert(request.__add_event_category.map((category_id) => ({budget_id: request.getId(), category_id: category_id})))
        
        if (request.__add_event_tag.length > 0)
            await this.connector('budget_tags').insert(request.__add_event_tag.map((tag_id) => ({budget_id: request.getId(), tag_id: tag_id})))
    }
}
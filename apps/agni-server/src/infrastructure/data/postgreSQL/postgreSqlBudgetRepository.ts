import { BudgetRepository } from "@core/repositories/budgetRepository";
import { KnexConnector } from "./postgreSqlConnector";
import { Knex } from "knex";
import { Budget } from "@core/domains/entities/budget";
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
    }

    async get(id: string): Promise<Budget> {
        const result = await this.connector('budgets').where('budget_id', id).first();
        if (!result) throw new ResourceNotFoundError(`Budget ${id} not found`);
        let budget = new Budget(
            result['budget_id'],
            result['is_archived'],
            result['target'],
            result['title'],
            result['date_start'],
            result['period'],
            result['period_time'],
            result['date_end'],
            result['date_update'], 
        )
        
        return budget
    }

    async getAll(): Promise<Budget[]> {
        const results = await this.connector('budgets');

        let budgets: Budget[] = []
        for(let result of results) {

            let budget = new Budget(
                result['budget_id'],
                result['is_archived'],
                result['target'],
                result['title'],
                result['date_start'],
                result['period'],
                result['period_time'],
                result['date_end'],
                result['date_update'],
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
    }

    async isBudgetExistByIds(ids: string[]): Promise<boolean> {
        let result = await this.connector('budgets').whereIn('budget_id', ids)

        return result.length === ids.length
    }
}
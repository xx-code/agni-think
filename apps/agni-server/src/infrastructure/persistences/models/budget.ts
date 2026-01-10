import { Knex } from "knex";
import Mapper, { KnexFilterExtendAdapter, KnexTable } from "./mapper";
import { Budget } from "@core/domains/entities/budget";
import { Scheduler } from "@core/domains/valueObjects/scheduleInfo";
import { KnexModel } from "./model";
import { BudgetFilter } from "@core/adapters/repository";

export class KnexBudgetTable implements KnexTable {
    getTableName(): string {
        return 'budgets'
    }

    async createTable(knex: Knex): Promise<void> {
        if (!(await knex.schema.hasTable('budgets')))
            await knex.schema.createTable('budgets', (table) => {
                table.uuid('budget_id').primary()
                table.string('title')
                table.float('target')
                table.json('scheduler')
                table.jsonb('save_goal_ids')
                table.boolean('is_archived')
            });  
    }
}

export type BudgetModel = KnexModel & {
    budget_id: string
    title: string
    target: number
    scheduler: any
    save_goal_ids: any
    is_archived: boolean
}

export class BudgetModelMapper implements Mapper<Budget, BudgetModel> {
    toDomain(model: BudgetModel): Budget {
        return new Budget(
            model.budget_id,
            model.is_archived,
            model.target,
            model.title,
            Scheduler.fromJson(model. scheduler),
            model.save_goal_ids ? Array.from(model.save_goal_ids) : []
        ) 
    }
    fromDomain(entity: Budget): BudgetModel {
        return {
            budget_id: entity.getId(),
            title: entity.getTitle(),
            target: entity.getTarget(),
            scheduler: entity.getSchedule().toJson(),  
            is_archived: entity.getIsArchive(),
            save_goal_ids: JSON.stringify(entity.getSaveGoalIds())
        }
    }
    getSortFilterFields(): string[] {
        return ['target']
    }
    getIdField(): string {
        return 'budget_id'
    }
    getNameField(): string {
        return 'title'
    }
}

export class BudgetFilterExtends implements KnexFilterExtendAdapter<Budget, BudgetModel> {
    filterQuery(query: Knex.QueryBuilder, filtersExtend: BudgetFilter): void {
        if (filtersExtend.schedulerDueDate) {
            switch(filtersExtend.schedulerDueDate.comparator) {
                case "<": 
                    query.whereRaw("(scheduler->>'due_date')::timestamp < ?", [filtersExtend.schedulerDueDate.date])
                    break
                case "<=":
                    query.whereRaw("(scheduler->>'due_date')::timestamp <= ?", [filtersExtend.schedulerDueDate.date])
                    break
                case ">":
                    query.whereRaw("(scheduler->>'due_date')::timestamp > ?", [filtersExtend.schedulerDueDate.date])
                    break
                case ">=":
                    query.whereRaw("(scheduler->>'due_date')::timestamp >= ?", [filtersExtend.schedulerDueDate.date])
                    break
                case "=":
                    query.whereRaw("(scheduler->>'due_date')::timestamp = ?", [filtersExtend.schedulerDueDate.date])
                    break
            }
        }
    }
}
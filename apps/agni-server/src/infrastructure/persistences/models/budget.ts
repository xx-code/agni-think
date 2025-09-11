import { Knex } from "knex";
import Mapper, { KnexTable } from "./mapper";
import { Budget } from "@core/domains/entities/budget";
import { Scheduler } from "@core/domains/valueObjects/scheduleInfo";
import { KnexModel } from "./model";

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
                table.boolean('is_archived')
            });  
    }
}

export type BudgetModel = KnexModel & {
    budget_id: string
    title: string
    target: number
    scheduler: any
    is_archived: boolean
}

export class BudgetModelMapper implements Mapper<Budget, BudgetModel> {
    toDomain(model: BudgetModel): Budget {
        return new Budget(
            model.budget_id,
            model.is_archived,
            model.target,
            model.title,
            Scheduler.fromJson(model. scheduler) 
        ) 
    }
    fromDomain(entity: Budget): BudgetModel {
        return {
            budget_id: entity.getId(),
            title: entity.getTitle(),
            target: entity.getTarget(),
            scheduler: entity.getSchedule().toJson(),  
            is_archived: entity.getIsArchive()
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
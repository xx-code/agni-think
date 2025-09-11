import { SaveGoal } from "@core/domains/entities/saveGoal";
import { Knex } from "knex";
import Mapper, { KnexTable } from "./mapper";
import { Money } from "@core/domains/entities/money";
import { KnexModel } from "./model";

export class KnexSaveGoalTable implements KnexTable {
    getTableName(): string {
        return 'save_goals'
    }
    async createTable(knex: Knex): Promise<void> {
        if (!(await knex.schema.hasTable('save_goals')))
            await knex.schema.createTable('save_goals', (table) => {
                table.uuid('save_goal_id').primary()
                table.string('title')
                table.float('target')
                table.float('balance')
                table.integer('desir_value')
                table.integer('importance')
                table.date('wish_due_date').nullable()
                table.string('description')
            });
    }
}

export type SaveGoalModel = KnexModel & {
    save_goal_id: string 
    title: string
    target: number
    balance: number
    desir_value: number
    importance: number
    wish_due_date?: Date
    description: string
}

export class SaveGoalModelMapper implements Mapper<SaveGoal, SaveGoalModel> {
    toDomain(model: SaveGoalModel): SaveGoal {
        return new SaveGoal(
            model.save_goal_id,
            model.title,
            new Money(model.target),
            new Money(model.balance),
            model.desir_value,
            model.importance, 
            model.wish_due_date,
            [],
            model.description
        )
    }
    fromDomain(entity: SaveGoal): SaveGoalModel {
        return {
            save_goal_id: entity.getId(),
            title: entity.getTitle(),
            target: entity.getTarget().getAmount(),
            balance: entity.getBalance().getAmount(),
            description: entity.getDescription(),
            desir_value: entity.getDesirValue(),
            wish_due_date: entity.getWishDueDate(),
            importance: entity.getImportance()
        }
    }
    getSortFilterFields(): string[] {
        return ['wish_due_date', 'balance', 'target']
    }
    getIdField(): string {
        return 'save_goal_id'
    }
    getNameField(): string {
        return 'title'
    }
}
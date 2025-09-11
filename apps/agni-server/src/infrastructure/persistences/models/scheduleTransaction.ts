import { ScheduleTransaction } from "@core/domains/entities/scheduleTransaction";
import { Knex } from "knex";
import Mapper, { KnexTable } from "./mapper";
import { Money } from "@core/domains/entities/money";
import { mapperMainTransactionCategory } from "@core/domains/constants";
import { Scheduler } from "@core/domains/valueObjects/scheduleInfo";
import { KnexModel } from "./model";

export class KnexScheduleTransactionTable implements KnexTable {
    getTableName(): string {
        return 'schedule_transactions' 
    }

    async createTable(knex: Knex): Promise<void> {
        if (!(await knex.schema.hasTable('schedule_transactions')))
            await knex.schema.createTable('schedule_transactions', (table) => {
                table.uuid('schedule_transaction_id').primary()
                table.uuid('account_id')
                table.uuid('category_id')
                table.float('amount')
                table.string('name')
                table.string('type')
                table.boolean('is_pause')
                table.boolean('is_pay')
                table.boolean('is_freeze')
                table.json('scheduler')
                table.jsonb('tag_ids')
            });
    }

}

export type ScheduleTransactionModel = KnexModel & {
    schedule_transaction_id: string
    account_id: string
    category_id: string
    amount: number
    name: string
    type: string
    is_pause: boolean
    is_pay: boolean
    is_freeze: boolean
    scheduler: any
    tag_ids: any
}

export class ScheduleTransactionMapper implements Mapper<ScheduleTransaction, ScheduleTransactionModel> {
    toDomain(model: ScheduleTransactionModel): ScheduleTransaction {
        return new ScheduleTransaction(
            model.schedule_transaction_id,
            model.name,
            model.account_id,
            model.category_id,
            new Money(model.amount),
            mapperMainTransactionCategory(model.type),
            Scheduler.fromJson(model.scheduler),
            model.is_pay,
            model.is_pause,
            model.is_freeze,
            model.tag_ids ? Array.from(model.tag_ids) : []
        );
    }
    fromDomain(entity: ScheduleTransaction): ScheduleTransactionModel {
        return {
            schedule_transaction_id: entity.getId(),
            account_id: entity.getAccountRef(),
            category_id: entity.getCategoryRef(),
            amount: entity.getAmount().getAmount(),
            name: entity.getName(),
            type: entity.getTransactionType(),
            is_pause: entity.getIsPause(),
            is_pay: entity.getIsPay(),
            is_freeze: entity.getIsFreeze(),
            scheduler: entity.getSchedule().toJson(),
            tag_ids: JSON.stringify(entity.getTags()) 
        }
    }
    getSortFilterFields(): string[] {
        return []
    }
    getIdField(): string {
        return 'schedule_transaction_id'
    }
    getNameField(): string {
        return 'name'
    }
}
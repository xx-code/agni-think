import { Record } from "@core/domains/entities/record";
import Mapper, { KnexTable } from "./mapper";
import { Knex } from "knex";
import { Money } from "@core/domains/entities/money";
import { mapperTransactionType } from "@core/domains/constants";
import { KnexModel } from "./model";

export class KnexRecordTable implements KnexTable {
    getTableName(): string {
        return 'records'
    }
    async createTable(knex: Knex): Promise<void> {
         if (!(await knex.schema.hasTable('records')))
            await knex.schema.createTable('records', (table) => {
                table.uuid('record_id').primary()
                table.float('money_amount')
                table.date('date')
                table.string('type')
                table.string('description')
            });
    }
}

export async function createRecordTable(knex: Knex) {
    if (!(await knex.schema.hasTable('records')))
        await knex.schema.createTable('records', (table) => {
            table.uuid('record_id').primary()
            table.float('money_amount')
            table.date('date')
            table.string('type')
            table.string('description')
        });
}

export type RecordModel = KnexModel & {
    record_id: string
    money_amount: number
    date: Date
    type: string
    description: string
}

export class RecordModelMapper implements Mapper<Record, RecordModel> {
    toDomain(model: RecordModel): Record {
        return new Record(
            model.record_id, 
            new Money(model.money_amount), 
            model.date, 
            mapperTransactionType(model.type),
            model.description
        )
    }
    fromDomain(entity: Record): RecordModel {
        return {
            record_id: entity.getId(),
            description: entity.getDescription(),
            date: entity.getUTCDate(),
            type: entity.getType(),
            money_amount: entity.getMoney().getAmount()
        }
    }
    getSortFilterFields(): string[] {
        return ['date']
    }
    getIdField(): string {
        return 'record_id'
    }
    getNameField(): string {
        return 'description'
    }

}
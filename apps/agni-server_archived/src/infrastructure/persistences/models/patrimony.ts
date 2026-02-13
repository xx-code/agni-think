import { Knex } from "knex";
import Mapper, { KnexTable } from "./mapper";
import { Patrimony } from "@core/domains/entities/patrimony";
import { mapperPatrimonyType } from "@core/domains/constants";
import { KnexModel } from "./model";

export class KnexPatrimonyTable implements KnexTable {
    getTableName(): string {
        return 'patrimonies'
    }

    async createTable(knex: Knex): Promise<void> {
        if (!(await knex.schema.hasTable('patrimonies')))
            await knex.schema.createTable('patrimonies', (table) => {
                table.uuid('patrimony_id').primary()
                table.string('title')
                table.float('amount')
                table.string('type')
                table.jsonb('account_ids')
                table.date('created_at')
                table.date('updated_at')
            });
    }

}

export type PatrimonyModel = KnexModel & {
    patrimony_id: string
    title: string
    amount: number
    type: string
    account_ids: any
    created_at: Date
    updated_at: Date
}

export class PatrimonyModelMapper implements Mapper<Patrimony, PatrimonyModel> {
    toDomain(model: PatrimonyModel): Patrimony {
        return new Patrimony(
            model.patrimony_id, 
            model.title, 
            model.amount,
            mapperPatrimonyType(model.type),
            model.account_ids ? Array.from(model.account_ids) : []
        ) 
    }
    fromDomain(entity: Patrimony): PatrimonyModel {
        return {
            patrimony_id: entity.getId(),
            title: entity.getTitle(),
            type: entity.getType(),
            amount: entity.getAmount(),
            account_ids: JSON.stringify(entity.getAccounts().map(i => i.accountId)),
            created_at: entity.getCreatedAt(),
            updated_at: entity.getUpdatedAt()
        }
    }
    getSortFilterFields(): string[] {
        return ['created_at', 'updated_at']
    }
    getIdField(): string {
        return 'patrimony_id'
    }
    getNameField(): string {
        return 'title'
    }
}
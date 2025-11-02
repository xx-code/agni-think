import { Account } from "@core/domains/entities/account";
import Mapper, { KnexTable } from "./mapper";
import { Knex } from "knex";
import { mapperTypeAccount } from "@core/domains/constants";
import { KnexModel } from "./model";

export class KnexAccountTable implements KnexTable {
    getTableName(): string {
        return 'accounts'
    }

    async createTable(knex: Knex): Promise<void> {
        if (!(await knex.schema.hasTable('accounts')))
            await knex.schema.createTable("accounts", (table) => {
                table.uuid('account_id').primary()
                table.string('title')
                table.string('type')
                table.float('balance')
                table.float('credit_limit').defaultTo(0)
            });
    }
}

export type AccountModel = KnexModel & {
    account_id: string
    title: string
    type: string
    balance: number
    credit_limit: number
}

export class AccountModelMapper implements Mapper<Account, AccountModel> {
    toDomain(model: AccountModel): Account {
        return new Account(
            model.account_id, 
            model.title, 
            mapperTypeAccount(model.type),
            model.balance,
            model.credit_limit
        )
    }
    fromDomain(entity: Account): AccountModel {
        return {
            account_id: entity.getId(),
            title: entity.getTitle(),
            balance: entity.getBalance(),
            credit_limit: entity.getCreditLimit(),
            type: entity.getType()
        }
    }
    getSortFilterFields(): string[] {
        return ['balance', 'credit_limit']
    }
    getIdField(): string {
        return 'account_id'
    }
    getNameField(): string {
        return 'title'
    }
} 

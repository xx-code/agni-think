import { Account } from "@core/domains/entities/account";
import Mapper, { KnexTable } from "./mapper";
import { Knex } from "knex";
import { AccountType, mapperTypeAccount } from "@core/domains/constants";
import { KnexModel } from "./model";
import { BrockageAccountDetail } from "@core/domains/valueObjects/brockageAccount";
import { CreditCardAccountDetail } from "@core/domains/valueObjects/creditCardAccount";

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
                table.string('currency_id')
                table.json('detail').nullable() 
                // table.float('credit_limit').defaultTo(0)
            });
    }
}

export type AccountModel = KnexModel & {
    account_id: string
    title: string
    type: string
    balance: number
    currency_id: string
    detail?: any
}

export class AccountModelMapper implements Mapper<Account, AccountModel> {
    toDomain(model: AccountModel): Account {
        let detail = undefined
        switch(model.type) {
            case AccountType.BROKING:
                detail = BrockageAccountDetail.fromJson(model.detail)
                break
            case AccountType.CREDIT_CARD:
                detail = CreditCardAccountDetail.fromJson(model.detail)
                break
            default:
                break
        }
        return new Account(
            model.account_id, 
            model.title, 
            mapperTypeAccount(model.type),
            model.currency_id,
            detail,
            model.balance,
        )
    }

    fromDomain(entity: Account): AccountModel {
        return {
            account_id: entity.getId(),
            title: entity.getTitle(),
            balance: entity.getBalance(),
            currency_id: entity.getCurrencyId(),
            detail: entity.getDetail()?.getJson(),
            type: entity.getType()
        }
    }

    getSortFilterFields(): string[] {
        return ['balance']
    }
    getIdField(): string {
        return 'account_id'
    }
    getNameField(): string {
        return 'title'
    }
} 

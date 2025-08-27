import { mapperTypeAccount } from "@core/domains/constants";
import { Account } from "@core/domains/entities/account";
import { isEmpty } from "@core/domains/helpers";
import { RepositoryError } from "@core/errors/repositoryError";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { AccountRepository } from "@core/repositories/accountRepository";
import knex, { Knex } from 'knex'
import { KnexConnector } from "./postgreSqlConnector";

export class PostgreSqlAccountRepository extends KnexConnector implements AccountRepository {
    constructor(connector: Knex) {
        super(connector)
    } 

    async initialisation() {
        try {
            let isExist = await this.connector.schema.hasTable('accounts')
            if (!isExist) {
                await this.connector.schema.createTable("accounts", (table) => {
                    table.uuid('account_id').primary()
                    table.string('title')
                    table.string('type')
                    table.float('balance')
                })
            }
        } catch (err: any) {
            throw new RepositoryError(err)
        }
    }

    async save(account: Account): Promise<void> {
        await this.connector('accounts').insert({ 
            account_id: account.getId(), 
            title: account.getTitle(), 
            type: account.getType(), 
            balance: account.getBalance() 
        }) 
    }

    async isExistByName(account_title: string): Promise<boolean> {
        let result = await this.connector('accounts').where('title', 'like', `${account_title}`).select('account_id').first()
        return !isEmpty(result)
    }

    async isExistById(id: string): Promise<boolean> {
        let result = await this.connector('accounts').select('*').where('account_id', id).first()
        return !isEmpty(result)
    }

    async isExistByIds(ids: string[]): Promise<boolean> {
        let result = await this.connector('accounts').whereIn('account_id', ids).select('account_id')
        return result.length === ids.length 
    }

    async get(id: string): Promise<Account> {
        let result = await this.connector('accounts').select('*').where('account_id', id).first()

        if (!result)
            throw new ResourceNotFoundError('Account not found')

        let account = new Account(result['account_id'], result['title'], mapperTypeAccount(result['type']), result['balance'])

        return account   
    }

    async getManyIds(ids: string[]): Promise<Account[]> {
        let results = await this.connector('accounts').whereIn('account_id', ids).select('*')

        return results.map(result => new Account(
                    result['account_id'], result['title'], 
                    mapperTypeAccount(result['type']), result['balance']))
    }

    async getAll(): Promise<Account[]> {
        let results = await this.connector('accounts').select('*')
        let accounts = results.map(result => (new Account(result['account_id'], result['title'], mapperTypeAccount(result['type']), result['balance'])))

        return accounts
    }

    async delete(id: string): Promise<void> {
        await this.connector('accounts').del().where('account_id', '=', id)
    }

    async update(account: Account): Promise<void> {
        await this.connector('accounts').where('account_id',  account.getId()).update({
            title: account.getTitle(),
            type: account.getType(),
            balance: account.getBalance()
        })
    }

}
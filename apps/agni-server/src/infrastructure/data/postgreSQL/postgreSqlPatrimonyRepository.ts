import { Patrimony } from "@core/domains/entities/patrimony";
import { QueryFilterAllRepository, RepositoryListResult } from "@core/repositories/dto";
import { PatrimonyRepository } from "@core/repositories/patrimonyRepository";
import { KnexConnector } from "./postgreSqlConnector";
import { Knex } from "knex";
import { isEmpty } from "@core/domains/helpers";
import { PatrimonyAccount } from "@core/domains/valueObjects/patrimonyAccount";
import { mapperPatrimonyType } from "@core/domains/constants";

export class PostgreSqlPatrimonyRepository extends KnexConnector implements PatrimonyRepository {
    constructor(connector: Knex) {
        super(connector)
    }

    initialisation(): Promise<void> {
        throw new Error("Method not implemented.");
    }

    async save(patrimony: Patrimony): Promise<void> {
        await this.connector('patrimonies').insert({
            patrimony_id: patrimony.getId(),
            title: patrimony.getTitle(),
            type: patrimony.getType(),
            amount: patrimony.getAmount(),
            created_at: patrimony.getCreatedAt(),
            updated_at: patrimony.getUpdatedAt()
        });

        if (patrimony.getAccounts().length > 0) {
            await this.connector('patrimony_accounts').insert(
                patrimony.getAccounts().map(acc => ({
                    patrimony_id: patrimony.getId(),
                    account_id: acc.accountId
                }))
            );
        }
    }

    async exist(id: string): Promise<boolean> {
        let result = await this.connector('patrimonies').where('patrimony_id', id).first();
        return !isEmpty(result)
    }

    async existByName(name: string): Promise<boolean> {
        let result = await this.connector('patrimonies').where('title', name).first();
        return !isEmpty(result);
    }

    async get(id: string): Promise<Patrimony | null> {
        let result = await this.connector('patrimonies').where('patrimony_id', id).first();
        if (!result)
            return null

        let patrimonyAccountResults = await this.connector('patrimony_accounts')
            .where('patrimony_id', id).select('account_id');
        let accounts = patrimonyAccountResults.map(row => new PatrimonyAccount(row.account_id));

        return new Patrimony(
            result['patrimony_id'], 
            result['title'], 
            result['amount'],
            mapperPatrimonyType(result['type']),
            accounts
        )
    }

    async getAll(queryFilter: QueryFilterAllRepository): Promise<RepositoryListResult<Patrimony>> {
        let query = this.connector('patrimonies').select('*')

        const total = await query.clone().clearSelect().clearOrder().count<{count: number}>("* as count").first() 
        const totalCount = total?.count ?? 0

        if (!queryFilter.queryAll)
            query.limit(queryFilter.limit).offset(queryFilter.offset)

        let results = await query;

        const patrimonies: Patrimony[] = []

        for(let i = 0; i < results.length; i++) {
            const result = results[i]

            let patrimonyAccountResults = await this.connector('patrimony_accounts')
                .where('patrimony_id', result['patrimony_id']).select('account_id');
            let accounts = patrimonyAccountResults.map(row => new PatrimonyAccount(row.account_id));

            const pat = new Patrimony(
                result['patrimony_id'], 
                result['title'], 
                result['amount'],
                mapperPatrimonyType(result['type']),
                accounts
            )
            
            patrimonies.push(pat)
        }

        return { items: patrimonies, total: totalCount }
    }

    async getManyById(ids: string[]): Promise<Patrimony[]> {
        let results = await this.connector('patrimonies').whereIn('patrimony_id', ids).select('*')

        const patrimonies: Patrimony[] = []

        for(let i = 0; i < results.length; i++) {
            const result = results[i]

            let patrimonyAccountResults = await this.connector('patrimony_accounts')
                .where('patrimony_id', result['patrimony_id']).select('account_id');
            let accounts = patrimonyAccountResults.map(row => new PatrimonyAccount(row.account_id));

            const pat = new Patrimony(
                result['patrimony_id'], 
                result['title'], 
                result['amount'],
                mapperPatrimonyType(result['type']),
                accounts
            )
            
            patrimonies.push(pat)
        }

        return patrimonies
    }

    async delete(id: string): Promise<void> {
        await this.connector('patrimonies').where('patrimony_id', id).delete()    
    }

    async update(patrimony: Patrimony): Promise<void> {
        await this.connector('patrimonies').where('patrimony_id', patrimony.getId()).update({
            title: patrimony.getTitle(),
            type: patrimony.getType(),
            amount: patrimony.getAmount(),
            updated_at: patrimony.getUpdatedAt()
        });

        await this.connector('patrimony_accounts').whereIn('account_id', patrimony.getAccountCollections().__deleted_object.map(i => i.accountId)).delete();
        if (patrimony.getAccountCollections().__added_object.length > 0)
            await this.connector('patrimony_accounts').insert(patrimony.getAccountCollections().__added_object.map(el => ({patrimony_id: patrimony.getId(), account_id: el.accountId})))
    }

}
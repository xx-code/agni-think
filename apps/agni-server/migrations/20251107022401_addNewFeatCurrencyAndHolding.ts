import { AccountType, ContributionAccountType, DOLLAR_CURRENT_ID, ManagementAccountType, mapperTypeAccount } from "@core/domains/constants";
import { BrockageAccountDetail } from "../../agni-server/src/core/domains/valueObjects/brockageAccount";
import { CreditCardAccountDetail } from "../../agni-server/src/core/domains/valueObjects/creditCardAccount";
import type { Knex } from "knex";


export async function up(knex: Knex): Promise<void> {

    if (!await knex.schema.hasColumn('accounts', 'currency_id')) {
        await knex.schema.alterTable('accounts', function(table) {
            table.uuid('currency_id').defaultTo(DOLLAR_CURRENT_ID)
            table.json('detail').nullable()
            table.dropColumn('credit_limit')
        })

        const results = await knex('accounts')
        for(let result of results) {
            const type = mapperTypeAccount(result['type'])
            let detail = null 
            switch(type) {
                case AccountType.CREDIT_CARD:
                    const cc = new CreditCardAccountDetail(0)
                    detail = cc.toJson()
                    break
                case AccountType.BROKING:
                    const broke = new BrockageAccountDetail(ManagementAccountType.MANAGED, ContributionAccountType.UNREGISTERED)
                    detail = broke.toJson()
                    break 
            } 
 
            await knex('accounts').where('account_id', '=', result['account_id']).update('detail', detail);
        } 
    }
}


export async function down(knex: Knex): Promise<void> {
    if ((await knex.schema.hasColumn('accounts', 'currency_id'))) {
        await knex.schema.alterTable('accounts', (table) => {
            table.dropColumn('currency_id')
            table.dropColumn('detail')
            table.float('credit_limit').defaultTo(0)
        })
    }
}


import type { Knex } from "knex";


export async function up(knex: Knex): Promise<void> {

    if (!await knex.schema.hasColumn('accounts', 'currency_id')) {
        await knex.schema.alterTable('accounts', function(table) {
            table.uuid('currency_id').defaultTo("4be50a1e-71b9-4941-b00a-ca8409949736")
            table.json('detail').nullable()
            table.dropColumn('credit_limit')
        })

        const results = await knex('accounts')
        for(let result of results) {
            let detail = null 
            switch(result['type']) {
                case "CreditCard":
                    detail = JSON.stringify({
                        credit_limit: 0
                    })
                    break
                case "Broking":
                    detail = JSON.stringify({
                        management_type: "Managed", 
                        contribution_account: "Unregistered" 
                    })
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


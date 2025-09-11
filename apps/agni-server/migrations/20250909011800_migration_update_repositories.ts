import type { Knex } from "knex";


export async function up(knex: Knex): Promise<void> {
    console.log("up 4")
    if (!(await knex.schema.hasColumn('accounts', 'credit_limit'))) {
        await knex.schema.alterTable('accounts', (table) => {
            table.integer('credit_limit').defaultTo(0)
        })
    }

    if (!(await knex.schema.hasColumn('patrimonies', 'account_ids'))) {
        await knex.schema.alterTable('patrimonies', async (table) => {
            table.jsonb('account_ids').defaultTo(JSON.stringify([])) 
        })
        const res = await knex('patrimonies').select('patrimony_id')
        const patrimonyIds = res.map(i => i.patrimony_id)
        for(let i = 0; i < patrimonyIds.length; i++) {
            const res = await knex('patrimony_accounts').select('*').where('patrimony_id', '=', patrimonyIds[i])
            if (res.length > 0)
                await knex('patrimonies').where('patrimony_id', '=', patrimonyIds[i]).update({
                    account_ids: JSON.stringify(res.map(i => i.account_id))
                })
        }
    }

    if (!(await knex.schema.hasColumn('schedule_transactions', 'tag_ids'))) {
        await knex.schema.alterTable('schedule_transactions', async (table) => {
            table.jsonb('tag_ids').defaultTo(JSON.stringify([])) 
        })
        const res = await knex('schedule_transaction_tags').select('schedule_transaction_id').distinct()
        const transactionIds = res.map(i => i.schedule_transaction_id)
        for(let i = 0; i < transactionIds.length; i++) {
            const res = await knex('schedule_transaction_tags').select('*').where('schedule_transaction_id', '=', transactionIds[i])
            if (res.length > 0)
                await knex('schedule_transactions').where('schedule_transaction_id', '=', transactionIds[i]).update({
                    tag_ids: JSON.stringify(res.map(i => i.tag_id))
                })
        }
    }

    if (!(await knex.schema.hasColumn('transactions', 'tag_ids'))) {
        await knex.schema.alterTable('transactions', async (table) => {
            table.jsonb('tag_ids').defaultTo(JSON.stringify([])) 
        })
        const res = await knex('transaction_tags').select('transaction_id').distinct()
        const transactionIds = res.map(i => i.transaction_id)
        for(let i = 0; i < transactionIds.length; i++) {
            const res = await knex('transaction_tags').select('*').where('transaction_id', '=', transactionIds[i])
            if (res.length > 0)
                await knex('transactions').where('transaction_id', '=', transactionIds[i]).update({
                    tag_ids: JSON.stringify(res.map(i => i.tag_id))
                })
        }
    }

    if (!(await knex.schema.hasColumn('transactions', 'budget_ids'))) {
        await knex.schema.alterTable('transactions', async (table) => {
            table.jsonb('budget_ids').defaultTo(JSON.stringify([]))
        })
        const res = await knex('transaction_budgets').select('transaction_id').distinct()
        const transactionIds = res.map(i => i.transaction_id)
        for(let i = 0; i < transactionIds.length; i++) {
            const res = await knex('transaction_budgets').select('*').where('transaction_id', '=', transactionIds[i])
            if (res.length > 0)
                await knex('transactions').where('transaction_id', '=', transactionIds[i]).update({
                    budget_ids: JSON.stringify(res.map(i => i.budget_id))
                })
        }
    }

}


export async function down(knex: Knex): Promise<void> {
    // Suppression de la colonne limit sur accounts
    if (await knex.schema.hasColumn('accounts', 'credit_limit')) {
        await knex.schema.alterTable('accounts', (table) => {
            table.dropColumn('credit_limit');
        });
    }

    // Suppression de la colonne account_ids sur patrimonies
    if (await knex.schema.hasColumn('patrimonies', 'account_ids')) {
        await knex.schema.alterTable('patrimonies', (table) => {
            table.dropColumn('account_ids');
        });
    }

    // Suppression de la colonne tag_ids sur schedule_transactions
    if (await knex.schema.hasColumn('schedule_transactions', 'tag_ids')) {
        await knex.schema.alterTable('schedule_transactions', (table) => {
            table.dropColumn('tag_ids');
        });
    }

    // Suppression de la colonne tag_ids sur transactions
    if (await knex.schema.hasColumn('transactions', 'tag_ids')) {
        await knex.schema.alterTable('transactions', (table) => {
            table.dropColumn('tag_ids');
        });
    }

    // Suppression de la colonne budget_ids sur transactions
    if (await knex.schema.hasColumn('transactions', 'budget_ids')) {
        await knex.schema.alterTable('transactions', (table) => {
            table.dropColumn('budget_ids');
        });
    }
}


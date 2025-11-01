import type { Knex } from "knex";


export async function up(knex: Knex): Promise<void> {
    if (!await knex.schema.hasColumn('save_goals', 'account_id')) {
        await knex.schema.alterTable('save_goals', function(table) {
            table.uuid('account_id').nullable()
        })
    }
}


export async function down(knex: Knex): Promise<void> {
    if ((await knex.schema.hasColumn('save_goals', 'account_id'))) {
        await knex.schema.alterTable('save_goals', (table) => {
            table.dropColumn('account_id')
        })
    }
}


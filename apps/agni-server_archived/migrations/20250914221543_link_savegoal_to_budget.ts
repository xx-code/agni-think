import type { Knex } from "knex";


export async function up(knex: Knex): Promise<void> {
    if (!await knex.schema.hasColumn('budgets', 'save_goal_ids')) {
        await knex.schema.alterTable('budgets', function(table) {
            table.jsonb('save_goal_ids').defaultTo(JSON.stringify([]))
        })
    }
}


export async function down(knex: Knex): Promise<void> {
    if ((await knex.schema.hasColumn('budgets', 'save_goal_ids'))) {
        await knex.schema.alterTable('budgets', (table) => {
            table.dropColumn('save_goal_ids')
        })
    }
}


import type { Knex } from "knex";


export async function up(knex: Knex): Promise<void> {
    if (!(await knex.schema.hasColumn('schedule_transactions', 'is_freeze'))) {
        await knex.schema.alterTable('schedule_transactions', (table) => {
            table.boolean('is_freeze').defaultTo(false)
        })
    }
}


export async function down(knex: Knex): Promise<void> {
    if ((await knex.schema.hasColumn('schedule_transactions', 'is_freeze'))) {
        await knex.schema.alterTable('schedule_transactions', (table) => {
            table.dropColumn('is_freeze')
        })
    }
}


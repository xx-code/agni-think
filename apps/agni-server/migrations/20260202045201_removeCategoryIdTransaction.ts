import type { Knex } from "knex";


export async function up(knex: Knex): Promise<void> {
    if (!(await knex.schema.hasColumn('transactions', 'category_id'))) {
        await knex.schema.alterTable('transactions', (table) => {
            table.dropColumn('category_id');
        });
    }
}


export async function down(knex: Knex): Promise<void> {
    if (await knex.schema.hasColumn('transactions', 'category_id')) {
        await knex.schema.alterTable('transactions', (table) => {
            table.uuid('category_id');
        });
    }
}


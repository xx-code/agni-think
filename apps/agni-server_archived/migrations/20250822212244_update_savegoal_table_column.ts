import type { Knex } from "knex";


export async function up(knex: Knex): Promise<void> {
    return knex.schema.table('save_goals', function(table) {
        table.text('description').alter(); 
    })
}


export async function down(knex: Knex): Promise<void> {
    return knex.schema.table('save_goals', function(table) {
        table.string('description').alter(); 
    });
}


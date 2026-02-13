import type { Knex } from "knex";

export async function up(knex: Knex): Promise<void> {
    if (!(await knex.schema.hasColumn('records', 'transaction_id'))) {
        // Get existing data before schema changes
        const transactions = await knex('transactions').select();
        const records = await knex('records')
            .whereIn('record_id', transactions.map(t => t.record_id));
        
        // Alter transactions table
        await knex.schema.alterTable('transactions', (table) => {
            table.dropColumn('tag_ids');
            table.dropColumn('budget_ids');
            table.dropColumn('record_id');
            table.jsonb('deductions').defaultTo(JSON.stringify([]));
        });
        
        // Alter records table
        await knex.schema.alterTable('records', (table) => {
            table.dropColumn('date');
            table.uuid('transaction_id');
            table.uuid('category_id');
            table.jsonb('tag_ids').defaultTo(JSON.stringify([]));
            table.jsonb('budget_ids').defaultTo(JSON.stringify([]));
        });
        
        // Migrate data in batch
        for (const trans of transactions) {
            const transRecords = records.filter(r => r.record_id === trans.record_id);
            
            for (const rc of transRecords) {
                await knex('records')
                    .where('record_id', rc.record_id)
                    .update({
                        transaction_id: trans.transaction_id,
                        category_id: trans.category_id,
                        budget_ids: JSON.stringify(trans.budget_ids) ,
                        tag_ids: JSON.stringify(trans.tag_ids) 
                    });
            }
        }
    }
}

export async function down(knex: Knex): Promise<void> {
    if (await knex.schema.hasColumn('records', 'transaction_id')) {
        // Get existing data before reverting
        const records = await knex('records').select();
        
        // Revert records table
        await knex.schema.alterTable('records', (table) => {
            table.dropColumn('transaction_id');
            table.dropColumn('category_id');
            table.dropColumn('tag_ids');
            table.dropColumn('budget_ids');
            table.timestamp('date').defaultTo(knex.fn.now());
        });
        
        // Revert transactions table
        await knex.schema.alterTable('transactions', (table) => {
            table.dropColumn('deductions');
            table.uuid('record_id');
            table.jsonb('tag_ids').defaultTo(JSON.stringify([]));
            table.jsonb('budget_ids').defaultTo(JSON.stringify([]));
        });
        
        // Restore data relationships
        for (const record of records) {
            if (record.transaction_id) {
                await knex('transactions')
                    .where('transaction_id', record.transaction_id)
                    .update({
                        record_id: record.record_id,
                        category_id: record.category_id,
                        tag_ids: record.tag_ids,
                        budget_ids: record.budget_ids
                    });
            }
        }
    }
}
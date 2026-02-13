import type { Knex } from "knex";


export async function up(knex: Knex): Promise<void> {
    if (await knex.schema.hasColumn('transactions', 'category_id')) {
        await knex.schema.alterTable('transactions', (table) => {
            table.dropColumn('category_id');
        });
    }

    if (!(await knex.schema.hasColumn('transactions', 'mouvement'))) {
       // Optimized 'up' migration logic
        await knex.schema.alterTable('transactions', (table) => {
            table.string('mouvement').nullable();
        });

        // Update all at once using a subquery
        await knex.raw(`
            UPDATE transactions 
            SET mouvement = (
                SELECT type 
                FROM records 
                WHERE records.transaction_id = transactions.transaction_id 
                LIMIT 1
            )
        `);

        // ONLY drop the column after the update is 100% done
        await knex.schema.alterTable('records', (table) => {
            table.dropColumn('type');
        });
    }
}


export async function down(knex: Knex): Promise<void> {
    // 1. Restore 'category_id' to 'transactions'
    if (!(await knex.schema.hasColumn('transactions', 'category_id'))) {
        await knex.schema.alterTable('transactions', (table) => {
            // Note: You may need to specify the type/constraints 
            // that originally existed for category_id here.
            table.integer('category_id').nullable(); 
        });
    }

    // 2. Restore 'type' column to 'records'
    if (!(await knex.schema.hasColumn('records', 'type'))) {
        await knex.schema.alterTable('records', (table) => {
            table.string('type');
        });

        // 3. Migrate data back from transactions.mouvement to records.type
        if (await knex.schema.hasColumn('transactions', 'mouvement')) {
            const transactions = await knex('transactions').select('transaction_id', 'mouvement');
            
            for (const transaction of transactions) {
                await knex('records')
                    .where('record_id', '=', transaction.transaction_id)
                    .update({ type: transaction.mouvement });
            }

            // 4. Drop 'mouvement' column now that data is restored
            await knex.schema.alterTable('transactions', (table) => {
                table.dropColumn('mouvement');
            });
        }
    }
}


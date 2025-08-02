import { TransactionStatus } from "@core/domains/constants";
import { table } from "console";
import type { Knex } from "knex";


export async function up(knex: Knex): Promise<void> {
    if (!(await knex.schema.hasColumn('budgets', 'scheduler'))) {
        await knex.schema.alterTable('budgets', (table) => {
            table.json('scheduler');

        })

        const results = await knex('budgets');
        for(let result of results) {
            const scheduler = {
                period: result['period'], 
                periodTime: result['period_item'],
                startedDate: result['date_start'],
                endingDate: result['date_end']
            }
            
            await knex('budgets').where('budget_id', '=', result['budget_id']).update('scheduler', JSON.stringify(scheduler));
        } 

        await knex.schema.alterTable('budgets', (table) => {
            table.dropColumn('date_start')
            table.dropColumn('date_update')
            table.dropColumn('date_end')
            table.dropColumn('period')
            table.dropColumn('period_time')
            table.dropColumn('is_system')
        });
    }
    
    if (!(await knex.schema.hasColumn('save_goal_items', 'item'))) {
        await knex.schema.alterTable('save_goal_items', (table) => {
            table.json('item') 
        });
        
        await knex.schema.alterTable('save_goal_items', (table) => {
            table.dropColumn('title')
            table.dropColumn('link')
            table.dropColumn('price')
            table.dropColumn('html_to_track')
        });
    }

    if (!(await knex.schema.hasColumn('transactions', 'status'))) {
        await knex.schema.alterTable('transactions', (table) => {
            table.string('status').defaultTo('Complete');
        })
    }
}


export async function down(knex: Knex): Promise<void> {
    // Budgets rollback
  const hasScheduler = await knex.schema.hasColumn('budgets', 'scheduler')
  if (hasScheduler) {
    await knex.schema.alterTable('budgets', (table) => {
      table.dropColumn('scheduler')
      table.date('date_start')
      table.date('date_update')
      table.date('date_end')
      table.string('period')
      table.string('period_time')
      table.boolean('is_system')
    })
  }

  // Save goal items rollback
  const hasItem = await knex.schema.hasColumn('save_goal_items', 'item')
  if (hasItem) {
    await knex.schema.alterTable('save_goal_items', (table) => {
      table.dropColumn('item')
      table.string('title')
      table.string('link')
      table.float('price')
      table.text('html_to_track')
    })
  }

  // Transactions rollback
  const hasStatus = await knex.schema.hasColumn('transactions', 'status')
  if (hasStatus) {
    await knex.schema.alterTable('transactions', (table) => {
      table.dropColumn('status')
    })
  }
}


import type { Knex } from "knex";

export async function up(knex: Knex): Promise<void> {
  console.log("up 2")
  if (!(await knex.schema.hasColumn('save_goals', 'desir_value'))) {
    await knex.schema.alterTable('save_goals', (table) => {
      table.integer('desir_value').defaultTo(0)
    })
  }
  
  if (!(await knex.schema.hasColumn('save_goals', 'importance'))) {
    await knex.schema.alterTable('save_goals', (table) => {
      table.integer('importance').defaultTo(1)
    })
  }

  if (!(await knex.schema.hasColumn('save_goals', 'wish_due_date'))) {
      await knex.schema.alterTable('save_goals', (table) => {
        table.date('wish_due_date')
      })
    }
}


export async function down(knex: Knex): Promise<void> {
  if ((await knex.schema.hasColumn('save_goals', 'desir_value'))) {
    await knex.schema.alterTable('save_goals', (table) => {
      table.dropColumn('desir_value')
    })
  }
  
  if ((await knex.schema.hasColumn('save_goals', 'importance'))) {
    await knex.schema.alterTable('save_goals', (table) => {
      table.dropColumn('importance')
    })
  }

  if ((await knex.schema.hasColumn('save_goals', 'wish_due_date'))) {
    await knex.schema.alterTable('save_goals', (table) => {
      table.dropColumn('wish_due_date')
    })
  }
}


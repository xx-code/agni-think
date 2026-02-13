import type { Knex } from "knex";


export async function up(knex: Knex): Promise<void> {
    const results = await knex('budgets');
    for(let result of results) {
        const object: {
            period: string
            periodTime?: number
            startedDate: string
            endingDate?: string
        } = result['scheduler']

        const newScheduler = {
            due_date: object.startedDate,
            repeater: object.periodTime ? {
                period: object.period, 
                interval: object.periodTime
            } : undefined
        }
        
        await knex('budgets').where('budget_id', '=', result['budget_id']).update('scheduler', JSON.stringify(newScheduler));
    } 

    if ((await knex.schema.hasColumn('schedule_transactions', 'is_pay'))) {
        await knex.schema.alterTable('schedule_transactions', (table) => {
            table.dropColumn('is_pay')
        })
    }

    const resultsTrans = await knex('schedule_transactions')
    for(let result of resultsTrans) {
        const object: {
            period: string
            periodTime?: number
            startedDate: string
            endingDate?: string
        } = result['scheduler']

        const newScheduler = {
            due_date: object.startedDate,
            repeater: object.periodTime ? {
                period: object.period, 
                interval: object.periodTime
            } : undefined
        }
        
        await knex('schedule_transactions').where('schedule_transaction_id', '=', result['schedule_transaction_id']).update('scheduler', JSON.stringify(newScheduler));
    } 
}


export async function down(knex: Knex): Promise<void> {
    const budgets = await knex('budgets');
    for(let budget of budgets) {
        const object: {
            due_date: string
            repeater?: {
                period: string
                interval: number
            }
        } = budget['scheduler']

        const oldScheduler = {
            period: object.repeater?.period || '',
            periodTime: object.repeater?.interval,
            startedDate: object.due_date,
            endingDate: undefined
        }
        
        await knex('budgets').where('budget_id', '=', budget['budget_id']).update('scheduler', JSON.stringify(oldScheduler));
    } 

    if (!(await knex.schema.hasColumn('schedule_transactions', 'is_pay'))) {
        await knex.schema.alterTable('schedule_transactions', (table) => {
            table.boolean('is_pay').defaultTo(false)
        })
    }

    const scheduleTransactions = await knex('schedule_transactions');
    for(let trans of scheduleTransactions) {
        const object: {
            due_date: string
            repeater?: {
                period: string
                interval: number
            }
        } = trans['scheduler']

        const oldScheduler = {
            period: object.repeater?.period || '',
            periodTime: object.repeater?.interval,
            startedDate: object.due_date,
            endingDate: undefined
        }
        
        await knex('schedule_transactions').where('schedule_transaction_id', '=', trans['schedule_transaction_id']).update('scheduler', JSON.stringify(oldScheduler));
    }
}


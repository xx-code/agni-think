import container, { DiContenair } from "./di_contenair";
import { Database } from "sqlite3";
import { open } from 'sqlite'
import { Account } from "@core/domains/entities/account";
import { Category } from "@core/domains/entities/category";
import { Record } from '@core/domains/entities/record';
import { Tag } from "@core/domains/entities/tag";
import { GetUID } from "@core/adapters/libs";
import { AccountType, mapperTransactionType, RecordType, TransactionStatus, TransactionType } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import { Transaction } from "@core/domains/entities/transaction";
import { SystemSeeder } from "./seeder";
import knex, { Knex } from "knex";
import dotenv from 'dotenv' 
dotenv.config()


const db: Knex = knex( {
  client: process.env.DB_CLIENT || '',
  connection: {
    host: process.env.DB_HOST || '',
    user: process.env.DB_USER || '',
    password: process.env.DB_PASSWORD || '',
    database : process.env.DB_NAME || ''
  }
})

export class DbMigration {
    private di: DiContenair 

    constructor(di: DiContenair) {
        this.di=di
    }

    async migrate() {
        let seeder = new SystemSeeder(this.di.getRepository('category'), this.di.getRepository('tag'))
        await seeder.seed()
        await this.migrationSQLLiteToPostgre() 
    }

    private async migrationSQLLiteToPostgre() {
        let sqliteDb = await open({
            filename: "database.db",
            driver: Database
        })

        await this.di.getRepository('unit_of_work').start()
        try {
            let results = await sqliteDb.all(`SELECT id, title, is_saving FROM accounts where is_saving = 0`)
            let all_accounts: Map<string, Account> = new Map()
            let all_categories: Map<string, Category> = new Map()
            let all_tags: Map<string, Tag> = new Map()
            let all_records: Map<string, Record> = new Map()


            for (let result of results) {
                let type = AccountType.CHECKING
                if (result['title'] === 'Perso')
                    type = AccountType.CHECKING

                if (result['title'] === 'Vehicule')
                    type = AccountType.BUSINESS

                if (result['title'] === 'Épargne')
                    type = AccountType.SAVING

                if (result['title'] === 'Depot Investissement')
                    type = AccountType.BROKING

                let account = new Account(GetUID(), result["title"], type)
                all_accounts.set(result["id"], account)
                await this.di.getRepository('account').save(account)
            } 

            // const creditDesj = new Account(GetUID(), 'Desjardin carte de credit', AccountType.CHECKING)
            // all_accounts.set('desj', creditDesj)

            // const creditCibc = new Account(GetUID(), 'Cibc carte de credit', AccountType.CHECKING)
            // all_accounts.set('cibc', creditCibc)

            results = await sqliteDb.all(`SELECT id, title, icon, color FROM categories`)
            for (let result of results) {
                if (result["id"] === "category-freeze" ||  result["id"] === "category-saving" || result["id"] === "category-transfert")
                    continue
                
                let category = new Category(GetUID(), result["title"], result["icon"], "#A9A9A9")

                all_categories.set(result["id"], category)
                await this.di.getRepository('category').save(category)
            }

            results = await sqliteDb.all(`SELECT id, value, color FROM tags`)
            for (let result of results) {
                let tag = new Tag(GetUID(), result["value"], "#A9A9A9")
                all_tags.set(result["id"], tag)
                await this.di.getRepository('tag').save(tag)
            }

            results = await sqliteDb.all(`SELECT id, amount, date, description, type FROM records`)
            const moment = require('moment');
            for (let result of results) {
                let money = new Money(result['amount'])
                let date = moment(result['date']).format("YYYY-MM-DDTHH:mm")
                let record = new Record(GetUID(), money, date, mapperTransactionType(result['type']), result['description'])
                all_records.set(result["id"], record)
            }

            results = await sqliteDb.all(`SELECT transactions.id as tran_id, id_account, id_category, id_record FROM transactions`)
            for (let result of results) {
                let category = all_categories.get(result["id_category"])
                let tags: Tag[] = []
                let tag_results = await sqliteDb.all(`SELECT id_transaction, id_tag FROM transactions_tags WHERE id_transaction = ?`, result['tran_id'])
                for (let tag_result of tag_results) {
                    let tag = all_tags.get(tag_result['id_tag'])
                    if (tag)
                        tags.push(tag)
                }
                let record = all_records.get(result["id_record"])

                let old_acc = all_accounts.get(result["id_account"])
                let is_Freeze = result["id_category"] === "category-freeze"
                if (old_acc && record && category && result["id_category"] !== 'category-saving') {

                    let account = await this.di.getRepository('account').get(old_acc.getId())

                    record.getType() === RecordType.CREDIT ? account.addOnBalance(record.getMoney()) : account.substractBalance(record.getMoney())
                    
                    await this.di.getRepository('record').save(record)

                    let typeTrans = TransactionType.VARIABLECOST
                    if (['LOYER', 'SANTÉ_ET_SOINS', 'TÉLÉPHONE', 'MAISON_ET_ENTRETIEN'].includes(category.getTitle()))
                        typeTrans = TransactionType.FIXEDCOST

                    if(['TRANSFERT', 'SAVING'].includes(category.getTitle())) 
                        typeTrans = TransactionType.OTHER

                    if (record.getType() === RecordType.CREDIT)
                        typeTrans = TransactionType.INCOME

                    let newTransaction = new Transaction(GetUID(), account.getId(), record.getId(), 
                    category.getId(), record.getUTCDate(), typeTrans, TransactionStatus.COMPLETE, tags.map((t) => t.getId()))
                    if (is_Freeze)
                        newTransaction.setIsFreeze()

                    await this.di.getRepository('account').update(account)
                    
                    await this.di.getRepository('transaction').save(newTransaction)
                } 
            }

            await this.di.getRepository('unit_of_work').commit()

            console.log("done")

            } 
            catch (err: any) {
            console.log(err)
            await this.di.getRepository('unit_of_work').rollback()
            sqliteDb.close()
        }
    }
}

async function main() {
    await container.config(db)
    const migrate = new DbMigration(container)

    await migrate.migrate()

    process.exit(1)
}

main()
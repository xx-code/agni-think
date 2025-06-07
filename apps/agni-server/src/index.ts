import { Database } from "sqlite3";
import { open } from 'sqlite'

import express, { application, Express, Request, Response } from "express";
import dotenv from "dotenv";
import bodyParser  = require("body-parser");
import cors from "cors"
import knex, { Knex } from "knex";
import { DiContenair } from "./di_contenair";
import { SystemSeeder } from './seeder';
const db: Knex = knex( {
  client: 'pg',
  connection: {
    host: 'localhost',
    user: "postgres",
    password: "root",
    database : "tests" //"agni-prod"
  }
}
)

export const diContainer = new DiContenair('postgreV1', db)

import AccountRoute from './routes/accounts';
import CategoryRoute from './routes/categories';
import TagRoute from './routes/tags';
import TransactionRoute from './routes/transactions';
import SaveGoalRoute from './routes/savingGoals';
import BudgetRoute from './routes/budgets';
import InternalRoute from './routes/internal';
import { Account } from "@core/domains/entities/account";
import { AccountType, FREEZE_CATEGORY_ID, mapperTransactionType, SAVING_CATEGORY_ID, TransactionMainCategory, TRANSFERT_CATEGORY_ID } from "@core/domains/constants";
import { Category } from "@core/domains/entities/category";
import { Tag } from "@core/domains/entities/tag";
import { Record, TransactionType } from "@core/domains/entities/record";
import { GetUID } from "@core/adapters/libs";
import { Money } from "@core/domains/entities/money";
import { PostgreSqlAccountRepository } from "@infra/data/postgreSQL/postgreSqlAccountRepository";
import { PostgreSqlCategoryRepository } from "@infra/data/postgreSQL/postgreSqlCategoryRepository";
import { PostgreSqlTagRepository } from "@infra/data/postgreSQL/postgreSqlTagRepository";
import { PostgreSqlUnitOfWork } from "@infra/data/postgreSQL/postgreSqlUnitOfWork";
import { PostgreSqlTransactionRepository } from "@infra/data/postgreSQL/postgreSqlTransactionRepository";
import { PostgreSqlRecordRepository } from "@infra/data/postgreSQL/postgreSqlRecordRepository";
import { Transaction } from "@core/domains/entities/transaction";

dotenv.config();

const app: Express = express();

const port = process.env.PORT || 3000;

const seeder = new SystemSeeder(diContainer.categoryRepository, diContainer.tagRepository)

seeder.seed()

app.use(bodyParser.urlencoded())
app.use(bodyParser.json())
app.use(cors({
  origin: 'http://localhost:3000', // Remplace '*' par l'origine exacte
  credentials: true
}
))



app.get("/", async (req: Request, res: Response) => {
  res.send("Agni server");
});

app.use(AccountRoute)
app.use(CategoryRoute)
app.use(TagRoute)
app.use(TransactionRoute)
app.use(SaveGoalRoute)
app.use(BudgetRoute)
app.use(InternalRoute)

app.listen(port, () => {
  console.log(`[server]: Server is running at http://localhost:${port}`);
});


app.get('/migration-test', async (req: Request, res: Response) => {
    let sqliteDb = await open({
      filename: "database.db",
      driver: Database
    })

    let uw = new PostgreSqlUnitOfWork(db)

    await uw.start()
    try {
      let results = await sqliteDb.all(`SELECT id, title, is_saving FROM accounts where is_saving = 0`)
      let all_accounts: Map<string, Account> = new Map()
      let all_categories: Map<string, Category> = new Map()
      let all_tags: Map<string, Tag> = new Map()
      let all_records: Map<string, Record> = new Map()


      let acc_pg = new PostgreSqlAccountRepository(db)
      let cat_pg = new PostgreSqlCategoryRepository(db)
      let tag_pg = new PostgreSqlTagRepository(db)
      let rec_pg = new PostgreSqlRecordRepository(db)
      let tran_pg = new PostgreSqlTransactionRepository(db)

      for (let result of results) {
        let account = new Account(GetUID(), result["title"], AccountType.CHECKING)
        all_accounts.set(result["id"], account)
        await acc_pg.save(account)
      } 

      results = await sqliteDb.all(`SELECT id, title, icon, color FROM categories`)
      for (let result of results) {
        let category
        if (result["id"] === "category-freeze")
          category = new Category(FREEZE_CATEGORY_ID, result["title"], result["icon"], "#A9A9A9")

        else if  (result["id"] === "category-saving")
          category = new Category(SAVING_CATEGORY_ID, result["title"], result["icon"], "#A9A9A9")

        else if (result["id"] === "category-transfert")
          category = new Category(TRANSFERT_CATEGORY_ID, result["title"], result["icon"], "#A9A9A9")
        else
          category = new Category(GetUID(), result["title"], result["icon"], "#A9A9A9")

        all_categories.set(result["id"], category)
        await cat_pg.save(category)
      }

      results = await sqliteDb.all(`SELECT id, value, color FROM tags`)
      for (let result of results) {
        let tag = new Tag(GetUID(), result["value"], "#A9A9A9")
        all_tags.set(result["id"], tag)
        await tag_pg.save(tag)
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

          let account = await acc_pg.get(old_acc.getId())

          record.getType() === TransactionType.CREDIT ? account.addOnBalance(record.getMoney()) : account.substractBalance(record.getMoney())
          
          await rec_pg.save(record)

          

          let newTransaction = new Transaction(GetUID(), account.getId(), record.getId(), category.getId(), record.getDate(), 
          TransactionMainCategory.FIXEDCOST, tags.map(t => t.getId()))
          if (is_Freeze)
            newTransaction.setIsFreeze()

          await acc_pg.update(account)
           
          await tran_pg.save(newTransaction)
        } 
      }

      await uw.commit()
      res.send("done")
    } catch (err: any) {
      console.log(err)
      await uw.rollback()
      res.send("Error")
    }
    
  }
)
import express, { application, Express, Request, Response } from "express";
import dotenv from "dotenv";
import bodyParser  = require("body-parser");
import cors from "cors"
import knex, { Knex } from "knex";
import { DiContenair } from "./di_contenair";
import { SystemSeeder } from './seeder';
const db: Knex = knex( {
  client: process.env.DB_CLIENT || '',
  connection: {
    host: process.env.DB_HOST || '',
    user: process.env.DB_USER || '',
    password: process.env.DB_PASSWORD || '',
    database : process.env.DB_NAME || ''//"agni-prod"
  }
})

export const diContainer = new DiContenair('postgreV1', db)

import AccountRoute from './routes/accounts';
import CategoryRoute from './routes/categories';
import TagRoute from './routes/tags';
import TransactionRoute from './routes/transactions';
import SaveGoalRoute from './routes/savingGoals';
import BudgetRoute from './routes/budgets';
import InternalRoute from './routes/internal';

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


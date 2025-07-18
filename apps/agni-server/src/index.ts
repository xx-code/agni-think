import express, { application, Express, Request, Response } from "express";
import dotenv from "dotenv";
import bodyParser  = require("body-parser");
import cors from "cors"
import knex, { Knex } from "knex";
import container from "./di_contenair";
import { SystemSeeder } from './seeder';
import AccountRoute from './routes/accounts';
import CategoryRoute from './routes/categories';
import TagRoute from './routes/tags';
import TransactionRoute from './routes/transactions';
import SaveGoalRoute from './routes/savingGoals';
import BudgetRoute from './routes/budgets';
import InternalRoute from './routes/internal';
dotenv.config();

const db: Knex = knex( {
  client: process.env.DB_CLIENT || '',
  connection: {
    host: process.env.DB_HOST || '',
    user: process.env.DB_USER || '',
    password: process.env.DB_PASSWORD || '',
    database : process.env.DB_NAME || ''//"agni-prod"
  }
})

const app: Express = express();

const port = process.env.PORT || 3000;

app.use(bodyParser.urlencoded())
app.use(bodyParser.json())
app.use(cors({
  origin: process.env.ORIGIN || '*', // Remplace '*' par l'origine exacte
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

app.listen(port, async () => {
  try {
    await container.config(db)

    const seeder = new SystemSeeder(container.getRepository('category'), container.getRepository('tag'))
    seeder.seed()
    console.log(`[server]: Server is running at http://localhost:${port}`);
    console.log(`[server]: Server db connected`);
  } catch(err) {
    console.log(err)
    console.log({
      host: process.env.DB_HOST || '',
      user: process.env.DB_USER || '',
      password: process.env.DB_PASSWORD || '',
      database: process.env.DB_NAME || ''
    });
  }
});


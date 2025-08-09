import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { KnexConnector } from "./postgreSqlConnector";
import { Knex } from "knex";

export class PostgreSqlUnitOfWork extends KnexConnector implements UnitOfWorkRepository {
    private transaction: Knex.Transaction | null = null;

    constructor(connector: Knex) {
        super(connector)
    }
    
    async initialisation(): Promise<void> {
        return
    }

    async start(): Promise<void> {
        if (this.transaction) {
            console.log("A transaction is already in progress.")
            return 
        }
        this.transaction = await this.connector.transaction();
    }

    async commit(): Promise<void> {
        if (!this.transaction) {
            console.log("No transaction in progress to commit.");
            return 
        }
        await this.transaction.commit();
        this.transaction = null;
    }
    
    async rollback(): Promise<void> {
        if (!this.transaction) {
            console.log("Transaction Error: No transaction in progress to rollback.");
            return
        }
        await this.transaction.rollback();
        this.transaction = null;
    }
}
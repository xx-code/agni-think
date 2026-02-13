import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { Knex } from "knex";

// TODO: To be redesign

export class PostgreSqlUnitOfWork implements UnitOfWorkRepository {
    private transaction: Knex.Transaction | null = null;
    private connector: Knex

    constructor(connector: Knex.Transaction) {
        this.connector = connector
    }
    
    async initialisation(): Promise<void> {
        return
    }

    async start(): Promise<any> {
        if (this.transaction) {
            console.log("A transaction is already in progress.")
            return 
        }
        this.transaction = await this.connector.transaction();
        return this.transaction
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
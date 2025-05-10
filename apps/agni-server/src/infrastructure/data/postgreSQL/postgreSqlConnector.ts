import { Knex } from "knex";

export abstract class KnexConnector{
    protected connector: Knex

    constructor(connector: Knex) {
        this.connector = connector
        this.initialisation()
    }

    
    abstract initialisation(): Promise<void>
}
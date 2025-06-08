import { Knex } from "knex";

export abstract class KnexConnector{
    protected connector: Knex

    constructor(connector: Knex) {
        this.connector = connector
    }

    
    abstract initialisation(): Promise<void>
}
import { Knex } from "knex";
import Mapper, { KnexFilterExtendAdapter, KnexTable } from "./mapper";
import { KnexModel } from "./model";
import { HoldingTransaction } from "@core/domains/entities/holdingTransaction";
import { HoldingTransactionExtendFilter } from "@core/adapters/repository";
import { Money } from "@core/domains/entities/money";
import { mapperHoldingTransactionType } from "@core/domains/constants";

export class KnexHoldingTransactionTable implements KnexTable {
    getTableName(): string {
        return 'holding_transactions'
    }

    async createTable(knex: Knex): Promise<void> {
        if (!(await knex.schema.hasTable('holding_transactions')))
            await knex.schema.createTable("holding_transactions", (table) => {
                table.uuid('holding_transaction_id').primary()
                table.uuid('holding_id')
                table.string('type')
                table.float('cost')
                table.float('quantity')
                table.float('fees')
                table.date('date')
                
                // table.float('credit_limit').defaultTo(0)
            });
    }
}

export type HoldingTransactionModel = KnexModel & {
    holding_transaction_id: string
    holding_id: string
    type: string
    cost: number
    quantity: number
    fees: number
    date: Date
}

export class HoldingTransactionModelMapper implements Mapper<HoldingTransaction, HoldingTransactionModel> {
    toDomain(model: HoldingTransactionModel): HoldingTransaction {
        return new HoldingTransaction(
            model.holding_transaction_id,
            model.holding_id,
            new Money(model.cost),
            model.quantity,
            model.fees,
            model.date,
            mapperHoldingTransactionType(model.type)
        )
    }

    fromDomain(entity: HoldingTransaction): HoldingTransactionModel {
        return {
            holding_transaction_id: entity.getId(),
            holding_id: entity.getHoldingId(),
            date: entity.getDate(),
            fees: entity.getFees(),
            quantity: entity.getQuantity(),
            type: entity.getType(),
            cost: entity.getCost().getAmount()
        }
    }

    getSortFilterFields(): string[] {
        return ['fees', 'quantity', 'cost']
    }
    getIdField(): string {
        return 'holding_transaction_id'
    }
    getNameField(): string {
        throw new Error("Method not implemented.")
    }
}

export class HoldingTransactionExtendFilterAdapter implements KnexFilterExtendAdapter<HoldingTransaction, HoldingTransactionModel> {
    filterQuery(query: Knex.QueryBuilder, filtersExtend: HoldingTransactionExtendFilter): void {
        if (filtersExtend.holdingId !== undefined)
            query.where('holding_id', '=', filtersExtend.holdingId)
    }
}
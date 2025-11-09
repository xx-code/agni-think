import { Knex } from "knex";
import Mapper, { KnexFilterExtendAdapter, KnexTable } from "./mapper";
import { KnexModel } from "./model";
import { Holding } from "@core/domains/entities/holding";
import { mapperHoldingType } from "@core/domains/constants";
import { HoldingExtendFilter } from "@core/adapters/repository";

export class KnexHoldingTable implements KnexTable {
    getTableName(): string {
        return 'holdings'
    }

    async createTable(knex: Knex): Promise<void> {
        if (!(await knex.schema.hasTable('holdings')))
            await knex.schema.createTable("holdings", (table) => {
                table.uuid('holding_id').primary()
                table.uuid('account_id')
                table.string('title')
                table.string('code')
                table.string('type')
                table.float('book_cost')
                table.float('quantity')
                table.float('current_price')
                table.date('last_market_value_update')
            });
    }
}

export type HoldingModel = KnexModel & {
    holding_id: string
    account_id: string
    title: string
    code: string
    type: string
    book_cost: number
    quantity: number
    current_price: number
    last_market_value_update: Date
}

export class HoldingModelMapper implements Mapper<Holding, HoldingModel> {
    toDomain(model: HoldingModel): Holding {
        return new Holding(
            model.holding_id,
            model.account_id,
            model.title,
            model.code,
            mapperHoldingType(model.type),
            model.last_market_value_update,
            model.book_cost,
            model.quantity,
            model.current_price
        )
    }

    fromDomain(entity: Holding): HoldingModel {
        return {
            holding_id: entity.getId(),
            account_id: entity.getAccountId(),
            title: entity.getTitle(),
            code: entity.getCode(),
            type: entity.getType(),
            book_cost: entity.getBookCost(),
            current_price: entity.getCurrentPrice(),
            quantity: entity.getQuantity(),
            last_market_value_update: entity.getLastUpdateMarketValue()
        }
    }

    getSortFilterFields(): string[] {
        return ['code', 'type', 'current_price', 'quantity']
    }
    getIdField(): string {
        return 'holding_id'
    }
    getNameField(): string {
        return 'title'
    }
}

export class HoldingExtendFilterAdapter implements KnexFilterExtendAdapter<Holding, HoldingModelMapper> {
    filterQuery(query: Knex.QueryBuilder, filtersExtend: HoldingExtendFilter): void {
        if (filtersExtend.accountId !== undefined)
            query.where('account_id', '=', filtersExtend.accountId)
    }
}
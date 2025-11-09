import { Knex } from "knex";
import Mapper, { KnexTable } from "./mapper";
import { KnexModel } from "./model";
import { Currency } from "@core/domains/entities/currency";

export class KnexCurrencyTable implements KnexTable {
    getTableName(): string {
        return 'currencies'
    }

    async createTable(knex: Knex): Promise<void> {
        if (!(await knex.schema.hasTable('currencies')))
            await knex.schema.createTable("currencies", (table) => {
                table.uuid('currency_id').primary()
                table.string('name')
                table.string('symbol')
                table.string('locale').nullable()
                table.float('rate_to_base').defaultTo(1)
                table.boolean('is_base').defaultTo(false)
                
                // table.float('credit_limit').defaultTo(0)
            });
    }
}

export type CurrencyModel = KnexModel & {
    currency_id: string
    name: string
    symbol: string
    locale?: string
    rate_to_base: number
    is_base: boolean
}

export class CurrencyModelMapper implements Mapper<Currency, CurrencyModel> {
    toDomain(model: CurrencyModel): Currency {
        return new Currency(
            model.currency_id,
            model.name,
            model.symbol,
            model.locale,
            model.rate_to_base,
            model.is_base
        )
    }

    fromDomain(entity: Currency): CurrencyModel {
        return {
            currency_id: entity.getId(),
            name: entity.getName(),
            is_base: entity.getIsBase() ?? false,
            rate_to_base: entity.getRateToBase() ?? 1,
            symbol: entity.getSymbol(),
            locale: entity.getLocale()
        }
    }

    getSortFilterFields(): string[] {
        return ['rate_to_base']
    }
    getIdField(): string {
        return 'currency_id'
    }
    getNameField(): string {
        return 'name'
    }
} 
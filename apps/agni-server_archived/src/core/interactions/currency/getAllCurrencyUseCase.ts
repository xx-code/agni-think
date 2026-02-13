import { ListDto, QueryFilter } from "@core/dto/base";
import { IUsecase } from "../interfaces";
import Repository from "@core/adapters/repository";
import { Currency } from "@core/domains/entities/currency";

export type GetAllCurrencyDto = {
    id: string
    name: string
    symbol: string
    locale?: string
    rateToBase?: number
    isBase?: boolean
}

export class GetAllCurrencyUseCase implements IUsecase<QueryFilter, ListDto<GetAllCurrencyDto>> {
    private currencyRepo: Repository<Currency>

    constructor(currencyRepo: Repository<Currency>) {
        this.currencyRepo = currencyRepo
    }

    async execute(request: QueryFilter): Promise<ListDto<GetAllCurrencyDto>> {
        const currencies = await this.currencyRepo.getAll(request)

        return {
            items: currencies.items.map(i => ({
                id: i.getId(),
                name: i.getName(),
                symbol: i.getSymbol(),
                locale: i.getLocale(),
                rateToBase: i.getRateToBase(),
                isBase: i.getIsBase(),
            })),
            totals: currencies.total
        }
    }
}
import Repository from "@core/adapters/repository"
import { IUsecase } from "../interfaces"
import { Currency } from "@core/domains/entities/currency"
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError"

export type GetCurrencyDto = {
    id: string
    name: string
    symbol: string
    locale?: string
    rateToBase?: number
    isBase?: boolean
}

export class GetCurrencyUseCase implements IUsecase<string, GetCurrencyDto> {
    private currencyRepo: Repository<Currency>

    constructor(currencyRepo: Repository<Currency>) {
        this.currencyRepo = currencyRepo
    }

    async execute(request: string): Promise<GetCurrencyDto> {
        const currency = await this.currencyRepo.get(request)
        if (currency === null)
            throw new ResourceNotFoundError("CURRENCY_NOT_FOUND")

        return {
            id: currency?.getId(),
            name: currency.getName(),
            symbol: currency.getSymbol(),
            locale: currency.getLocale(),
            rateToBase: currency.getRateToBase(),
            isBase: currency.getIsBase() 
        }
    }
}
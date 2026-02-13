import { CreatedDto } from "@core/dto/base"
import { IUsecase } from "../interfaces"
import Repository from "@core/adapters/repository"
import { Currency } from "@core/domains/entities/currency"
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError"
import { GetUID } from "@core/adapters/libs"

export type RequestCreateCurrency = {
    name: string
    symbol: string
    locale?: string
    rateToBase?: number
    isBase?: boolean
}

export class CreateCurrencyUseCase implements IUsecase<RequestCreateCurrency, CreatedDto> {
    private currencyRepo: Repository<Currency>

    constructor(currencyRepo: Repository<Currency>) {
        this.currencyRepo = currencyRepo
    }

    async execute(request: RequestCreateCurrency): Promise<CreatedDto> {
        if (await this.currencyRepo.existByName(request.name))
            throw new ResourceAlreadyExist("CURRENCY_ALREADY_EXIST")

        const currency = new Currency(
            GetUID(), request.name, 
            request.symbol, request.locale, request.rateToBase, request.isBase)

        await this.currencyRepo.create(currency)
        
        return {
            newId: currency.getId()
        }
    }
}
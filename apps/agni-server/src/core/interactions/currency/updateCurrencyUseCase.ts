import Repository from "@core/adapters/repository"
import { Currency } from "@core/domains/entities/currency"
import { IUsecase } from "../interfaces"
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError"
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError"
import { ValueError } from "@core/errors/valueError"

export type RequestUpdateCurrency = {
    id: string
    name?: string
    symbol?: string
    locale?: string
    rateToBase?: number
    isBase?: boolean
}

export class UpdateCurrencyUseCase implements IUsecase<RequestUpdateCurrency, void> {
    private currencyRepo: Repository<Currency>

    constructor(currencyRepo: Repository<Currency>) {
        this.currencyRepo = currencyRepo
    }

    async execute(request: RequestUpdateCurrency): Promise<void> {
        const currency = await this.currencyRepo.get(request.id)
        if (currency === null)
            throw new ResourceNotFoundError("CURRENCY_NOT_FOUND")

        if (request.name) {
            if (request.name.toLowerCase() === currency.getName().toLowerCase() && (await this.currencyRepo.existByName(request.name)))
                throw new ResourceAlreadyExist("CURRENCY_ALREADY_EXIST")

            currency.setName(request.name)
        }

        if (request.isBase) {
            const currencies = await this.currencyRepo.getAll({ limit: 0, offset: 0, queryAll: true})
            if (request.isBase === true && currencies.items.some(i => i.getIsBase() === true && i.getId() !== request.id))
                throw new ValueError("CURRENCY_HAVE_ALREADY_BASE")

            currency.setIsBase(request.isBase)
        }

        if (request.locale) {
            currency.setLocale(request.locale)
        }

        if (request.rateToBase) {
            currency.setRateToBase(request.rateToBase)
        }

        if (request.symbol) {
            currency.setSymbol(request.symbol)
        }

        await this.currencyRepo.update(currency)
    }
}
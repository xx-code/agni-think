import Repository from "@core/adapters/repository";
import { IUsecase } from "../interfaces";
import { Currency } from "@core/domains/entities/currency";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";

export class DeleteCurrencyUseCase implements IUsecase<string, void> {
    private currencyRepo: Repository<Currency>

    constructor(currencyRepo: Repository<Currency>) {
        this.currencyRepo = currencyRepo
    }

    async execute(request: string): Promise<void> {
        if (!await this.currencyRepo.get(request))
            throw new  ResourceNotFoundError("CURRENCY_NOT_FOUND")

        await this.currencyRepo.delete(request)
    }
}
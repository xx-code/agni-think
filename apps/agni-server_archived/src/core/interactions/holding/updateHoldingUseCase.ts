import { Holding } from "@core/domains/entities/holding"
import { IUsecase } from "../interfaces"
import Repository from "@core/adapters/repository"
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError"
import { AccountType, mapperHoldingType } from "@core/domains/constants"
import { Account } from "@core/domains/entities/account"
import UnExpectedError from "@core/errors/unExpectedError"

export type RequestUpdateHoldingDto = {
    id: string
    accountId?: string
    title?: string
    code?: string
    type?: string
    currentPrice?: number
}

export class UpdateHoldingUseCase implements IUsecase<RequestUpdateHoldingDto, void> {
    private holdingRepo: Repository<Holding>
    private accountRepo: Repository<Account>

    constructor(holdingRepo: Repository<Holding>, accountRepo: Repository<Account>) {
        this.holdingRepo = holdingRepo
        this.accountRepo = accountRepo
    }

    async execute(request: RequestUpdateHoldingDto): Promise<void> {
        const holding = await this.holdingRepo.get(request.id)
        if (holding === null)
            throw new ResourceNotFoundError("HOLDING_NOT_FOUND")

        if (request.accountId) {
            const account = await this.accountRepo.get(request.accountId)
            if (!account)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

            if (account.getType() !== AccountType.BROKING)
                throw new UnExpectedError("NOT_ACCOUNT_BROKAGE")
        }


        if (request.title) {
            if (request.title.toLowerCase() === holding.getTitle().toLowerCase() && 
                (await this.holdingRepo.existByName(request.title)))
                throw new ResourceNotFoundError("HOLDING_ALREADY_EXIST")

            holding.setTitle(request.title)
        }

        if (request.code) {
            holding.setCode(request.code)
        }

        if (request.currentPrice) {
            holding.setCurrentPrice(request.currentPrice)
            holding.setLastUpdateMarketValue(new Date())
        }

        if (request.type) {
            holding.setType(mapperHoldingType(request.type))
        }

        await this.holdingRepo.update(holding)
    }
}
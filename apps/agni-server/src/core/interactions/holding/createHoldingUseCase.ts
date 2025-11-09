import { CreatedDto } from "@core/dto/base";
import { IUsecase } from "../interfaces";
import Repository from "@core/adapters/repository";
import { Account } from "@core/domains/entities/account";
import { Holding } from "@core/domains/entities/holding";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { GetUID } from "@core/adapters/libs";
import { AccountType, mapperHoldingType } from "@core/domains/constants";
import UnExpectedError from "@core/errors/unExpectedError";

export type RequestCreateHoldingDto = {
    accountId: string
    title: string
    code: string
    type: string
    bookCost: number
    quantity: number
    currentPrice: number
}

export class CreateHoldingUseCase implements IUsecase<RequestCreateHoldingDto, CreatedDto> {
    private accountRepo: Repository<Account>
    private holdingRepo: Repository<Holding>

    constructor(accountRepo: Repository<Account>, holdingRepo: Repository<Holding>) {
        this.accountRepo = accountRepo
        this.holdingRepo = holdingRepo
    }

    async execute(request: RequestCreateHoldingDto): Promise<CreatedDto> {
        const account = await this.accountRepo.get(request.accountId)
        if (!account)
            throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

        if (account.getType() !== AccountType.BROKING)
            throw new UnExpectedError("NOT_ACCOUNT_BROKAGE")
        
        if (await this.holdingRepo.existByName(request.title))
            throw new ResourceNotFoundError("HOLDING_ALREADY_EXIST")

        const newHolding = new Holding(GetUID(), request.accountId,request.title, request.code, mapperHoldingType(request.type), 
            new Date(), request.bookCost, request.quantity, request.currentPrice)

        await this.holdingRepo.create(newHolding)
        
        return {
            newId: newHolding.getId()
        }
    }
}
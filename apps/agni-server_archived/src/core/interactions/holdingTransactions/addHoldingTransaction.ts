import { CreatedDto } from "@core/dto/base"
import { IUsecase } from "../interfaces"
import Repository from "@core/adapters/repository"
import { Holding } from "@core/domains/entities/holding"
import { HoldingTransaction } from "@core/domains/entities/holdingTransaction"
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError"
import { GetUID } from "@core/adapters/libs"
import { Money } from "@core/domains/entities/money"
import { HoldingTransactionType, mapperHoldingTransactionType, SAVING_CATEGORY_ID } from "@core/domains/constants"
import { Account } from "@core/domains/entities/account"
import { Transaction } from "@core/domains/entities/transaction"
import { RequestAddTransactionUseCase } from "../transaction/addTransactionUseCase"

export type RequestAddHoldingTransactionDto = {
    holdingId: string
    quantity: number
    fees: number
    date: Date
    cost: number
    type: string
}

export class AddHoldingTransactionUseCase implements IUsecase<RequestAddHoldingTransactionDto, CreatedDto> {
    private holdingRepo: Repository<Holding>
    private holdingTransactionRepo: Repository<HoldingTransaction>
    private accountRepo: Repository<Account>

    constructor(holdingRepo: Repository<Holding>, holdingTransRepo: Repository<HoldingTransaction>, 
        accountRepo: Repository<Account>) {
        this.holdingRepo = holdingRepo
        this.accountRepo = accountRepo
        this.holdingTransactionRepo = holdingTransRepo
    }

    async execute(request: RequestAddHoldingTransactionDto): Promise<CreatedDto> {
        const holding = await this.holdingRepo.get(request.holdingId)
        if (!holding)
            throw new ResourceNotFoundError("HOLDING_NOT_FOUND")


        const type = mapperHoldingTransactionType(request.type)

        if ((holding.getQuantity() < request.quantity)&& type === HoldingTransactionType.SELL)
            throw new ResourceNotFoundError("YOU_CANT_ADD_TRANSACTION_COST_CREATE_THAN_ACCOUNT")
        
        const newHoldingTransaction = new HoldingTransaction(
            GetUID(),
            request.holdingId,
            new Money(request.cost),
            request.quantity,
            request.fees,
            request.date,
            type
        ) 

        if (type === HoldingTransactionType.BUY || type === HoldingTransactionType.CONTRIBUTION 
            || type === HoldingTransactionType.DIVIDEND ) {
            holding.setBookCost(holding.getBookCost() + ((request.cost * request.quantity) + request.fees))
            holding.setQuantity(holding.getQuantity() + request.quantity)
        } else {
            holding.setBookCost(holding.getBookCost() - ((request.cost * request.quantity) + request.fees))
            holding.setQuantity(holding.getQuantity() - request.quantity)
        }

        await this.holdingTransactionRepo.create(newHoldingTransaction)
        await this.holdingRepo.update(holding)

        return {
            newId: newHoldingTransaction.getId()
        }
    }
}
import { HoldingTransactionType, mapperHoldingTransactionType } from "@core/domains/constants"
import { IUsecase } from "../interfaces"
import Repository from "@core/adapters/repository"
import { HoldingTransaction } from "@core/domains/entities/holdingTransaction"
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError"
import { Money } from "@core/domains/entities/money"
import { Holding } from "@core/domains/entities/holding"
import { Account } from "@core/domains/entities/account"
import { RequestAddHoldingTransactionDto } from "./addHoldingTransaction"
import { CreatedDto } from "@core/dto/base"

export type RequestUpdateHoldingTransactionDto = {
    id: string
    type?: string
    cost?: number
    quantity?: number
    fees?: number
    date?: Date
}

export class UpdateHoldingTransactionUseCase implements IUsecase<RequestUpdateHoldingTransactionDto, void> {
    private holdingTransactionRepo: Repository<HoldingTransaction>

    private addHoldingTransaction: IUsecase<RequestAddHoldingTransactionDto, CreatedDto>
    private deleteHoldingTransaction: IUsecase<string, void>

    constructor(holdingTransRepo: Repository<HoldingTransaction>,
        addHoldingTrans: IUsecase<RequestAddHoldingTransactionDto, CreatedDto>, deleteHoldingTrans: IUsecase<string, void>
    ) {
        this.holdingTransactionRepo = holdingTransRepo
        this.addHoldingTransaction = addHoldingTrans
        this.deleteHoldingTransaction = deleteHoldingTrans
    }

    async execute(request: RequestUpdateHoldingTransactionDto): Promise<void> {
        const holdingTrans = await this.holdingTransactionRepo.get(request.id)
        if (holdingTrans === null)
            throw new ResourceNotFoundError("HOLDING_TRANSACTION_NOT_FOUND") 

        await this.deleteHoldingTransaction.execute(holdingTrans.getId())
        
        await this.addHoldingTransaction.execute({
            cost: request.cost ?? holdingTrans.getCost().getAmount(), 
            date: request.date ?? holdingTrans.getDate(),
            fees: request.fees ?? holdingTrans.getFees(),
            quantity: request.quantity ?? holdingTrans.getQuantity(),
            type: request.type ?? holdingTrans.getType(),
            holdingId: holdingTrans.getHoldingId()
        })  
    } 
}
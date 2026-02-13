import { HoldingTransactionType } from "@core/domains/constants"
import { IUsecase } from "../interfaces"
import Repository from "@core/adapters/repository"
import { Holding } from "@core/domains/entities/holding"
import { HoldingTransaction } from "@core/domains/entities/holdingTransaction"
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError"

export class DeleteHoldingTransactionUseCase implements IUsecase<string, void> {
    private holdingRepo: Repository<Holding>
    private holdingTransactionRepo: Repository<HoldingTransaction>

    constructor(holdingRepo: Repository<Holding>, holdingTransRepo: Repository<HoldingTransaction>) {
        this.holdingRepo = holdingRepo
        this.holdingTransactionRepo = holdingTransRepo
    }

    async execute(request: string): Promise<void> {
        const trans = await this.holdingTransactionRepo.get(request)
        if (trans === null)
            throw new ResourceNotFoundError("HOLDING_TRANSACTION_NOT_FOUND")

        const holding = await this.holdingRepo.get(trans.getHoldingId())
        if (holding === null)
            throw new ResourceNotFoundError("HOLDING_NOT_FOUND")


        if (trans.getType() === HoldingTransactionType.BUY || trans.getType() === HoldingTransactionType.CONTRIBUTION 
            || trans.getType() === HoldingTransactionType.DIVIDEND ) {
            holding.setBookCost(holding.getBookCost() - ((trans.getCost().getAmount() * trans.getQuantity()) + trans.getFees()))
            holding.setQuantity(holding.getQuantity() - trans.getQuantity())
        } else {
            holding.setBookCost(holding.getBookCost() + ((trans.getCost().getAmount() * trans.getQuantity()) + trans.getFees()))
            holding.setQuantity(holding.getQuantity() + trans.getQuantity())
        }


        

        await this.holdingTransactionRepo.delete(request)
        
    }
}
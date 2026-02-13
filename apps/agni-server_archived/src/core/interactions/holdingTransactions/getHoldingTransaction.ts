import { IUsecase } from "../interfaces";
import Repository from "@core/adapters/repository";
import { HoldingTransaction } from "@core/domains/entities/holdingTransaction";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";


export type GetHoldingTransactionDto = {
    id: string
    holdingId: string
    cost: number
    fees: number
    quantity: number
    date: Date
}

export class GetHoldingTransactionUseCase implements IUsecase<string, GetHoldingTransactionDto> {
    private holdingTransactionRepo: Repository<HoldingTransaction>

    constructor(holdingTransactionRepo: Repository<HoldingTransaction>) {
        this.holdingTransactionRepo = holdingTransactionRepo
    }

    async execute(request: string): Promise<GetHoldingTransactionDto> {
        const holdingTransaction = await this.holdingTransactionRepo.get(request)
        if (holdingTransaction === null)
            throw new ResourceNotFoundError("HOLDING_TRANSACTION_NOT_FOUND")
        
        return {
            id: holdingTransaction.getId(),
            cost: holdingTransaction.getCost().getAmount(),
            date: holdingTransaction.getDate(),
            fees: holdingTransaction.getFees(),
            holdingId: holdingTransaction.getHoldingId(),
            quantity: holdingTransaction.getQuantity()
        }
    }
}
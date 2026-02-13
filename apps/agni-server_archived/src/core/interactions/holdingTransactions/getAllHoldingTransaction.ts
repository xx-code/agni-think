import { ListDto, QueryFilter } from "@core/dto/base";
import { IUsecase } from "../interfaces";
import Repository, { HoldingTransactionExtendFilter } from "@core/adapters/repository";
import { HoldingTransaction } from "@core/domains/entities/holdingTransaction";

export type HoldingTransactionQueryFilter = {
    holdingId: string
    query: QueryFilter
}

export type GetAllHoldingTransactionDto = {
    id: string
    holdingId: string
    cost: number
    type: string
    fees: number
    quantity: number
    date: Date
}

export class GetAllHoldingTransactionUseCase implements IUsecase<HoldingTransactionQueryFilter, ListDto<GetAllHoldingTransactionDto>> {
    private holdingTransactionRepo: Repository<HoldingTransaction>

    constructor(holdingTransactionRepo: Repository<HoldingTransaction>) {
        this.holdingTransactionRepo = holdingTransactionRepo
    }

    async execute(request: HoldingTransactionQueryFilter): Promise<ListDto<GetAllHoldingTransactionDto>> {
        const extendFilter = new HoldingTransactionExtendFilter()
        extendFilter.holdingId = request.holdingId

        const holdingTransactions = await this.holdingTransactionRepo.getAll(request.query, extendFilter)

        return {
            items: holdingTransactions.items.map(i => ({
                id: i.getId(),
                cost: i.getCost().getAmount(),
                date: i.getDate(),
                fees: i.getFees(),
                type: i.getType(),
                holdingId: i.getHoldingId(),
                quantity: i.getQuantity()
            })),
            totals: holdingTransactions.total
        }
    }
}
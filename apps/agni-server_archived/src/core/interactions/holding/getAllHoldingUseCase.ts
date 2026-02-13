import { ListDto, QueryFilter } from "@core/dto/base";
import { IUsecase } from "../interfaces";
import Repository, { HoldingExtendFilter } from "@core/adapters/repository";
import { Holding } from "@core/domains/entities/holding";

export type HoldingQueryFilter = {
    accountId?: string
    query: QueryFilter
}

export type GetAllHoldingDto = {
    id: string
    accountId: string
    title: string
    code: string
    type: string
    totalCost: number
    totalQuantity: number
    marketValue: number
    yield: number
    lastUpdateDate: Date
}

export class GetAllHoldingUseCase implements IUsecase<HoldingQueryFilter, ListDto<GetAllHoldingDto>> {
    private hodlingRepo: Repository<Holding>

    constructor(holdingRepo: Repository<Holding>) {
        this.hodlingRepo = holdingRepo
    }

    async execute(request: HoldingQueryFilter): Promise<ListDto<GetAllHoldingDto>> {
        const extendFilter = new HoldingExtendFilter()
        extendFilter.accountId = request.accountId

        const holdings = await this.hodlingRepo.getAll(request.query, extendFilter)

        return {
            items: holdings.items.map(i => ({
                id: i.getId(),
                accountId: i.getAccountId(),
                title: i.getTitle(),
                code: i.getCode(),
                lastUpdateDate: i.getLastUpdateMarketValue(),
                totalCost: i.getBookCost(),
                totalQuantity: i.getQuantity(),
                marketValue: i.getMarketValue(),
                type: i.getType(),
                yield: i.getTotalyield()
            })),
            totals: holdings.total
        }
    }
}
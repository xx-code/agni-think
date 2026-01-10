import { ListDto, QueryFilter } from "@core/dto/base"
import { IUsecase } from "../interfaces"
import Repository from "@core/adapters/repository"
import { ScheduleTransaction } from "@core/domains/entities/scheduleTransaction"

export type GetAllScheduleTransactionDto = {
    id: string
    name: string
    accountId: string
    categoryId: string
    tagIds: string[]
    type: string
    amount: number,
    isPause: boolean
    isFreeze: boolean
    dueDate: Date
    repeater?: {
        period: string
        interval: number
    }
}

export class GetAllScheluleTransacationUseCase implements IUsecase<QueryFilter, ListDto<GetAllScheduleTransactionDto>> {

    private scheduleTransactionRepo: Repository<ScheduleTransaction>

    constructor(scheduleTransactionRepo: Repository<ScheduleTransaction>) {
        this.scheduleTransactionRepo = scheduleTransactionRepo
    }

    async execute(request: QueryFilter): Promise<ListDto<GetAllScheduleTransactionDto>> {
        const scheduleTransactions = await this.scheduleTransactionRepo.getAll({
            limit: request.limit,
            offset: request.offset,
            queryAll: request.queryAll 
        })

        const response: GetAllScheduleTransactionDto[] = []
        scheduleTransactions.items.forEach(trans => {
            response.push({
                id: trans.getId(),
                name: trans.getName(),
                accountId: trans.getAccountRef(),
                categoryId: trans.getCategoryRef(),
                amount: trans.getAmount().getAmount(),
                tagIds: trans.getTags(),
                type: trans.getTransactionType(),
                isPause: trans.getIsPause(),
                isFreeze: trans.getIsFreeze(),
                dueDate: trans.getSchedule().dueDate,
                repeater: trans.getSchedule(). repeater ? {
                    period: trans.getSchedule().repeater!.period,
                    interval: trans.getSchedule().repeater!.interval
                } : undefined
            })
        })

        return { items: response, totals: scheduleTransactions.total }
    }

}
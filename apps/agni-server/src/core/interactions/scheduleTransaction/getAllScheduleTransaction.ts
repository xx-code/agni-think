import { ListDto } from "@core/dto/base"
import { IUsecase } from "../interfaces"
import { ScheduleTransactionRepository } from "@core/repositories/scheduleTransactionRepository"

export type GetAllScheduleTransactionDto = {
    id: string
    name: string
    accountId: string
    categoryId: string
    tagIds: string[]
    type: string
    isPause: boolean
    dateStart: string
    period: string
    dateUpdate: string
    periodTime?: number
    dateEnd?: string
}

export class GetAllScheluleTransacationUseCase implements IUsecase<void, ListDto<GetAllScheduleTransactionDto>> {

    private scheduleTransactionRepo: ScheduleTransactionRepository

    constructor(scheduleTransactionRepo: ScheduleTransactionRepository) {
        this.scheduleTransactionRepo = scheduleTransactionRepo
    }

    async execute(): Promise<ListDto<GetAllScheduleTransactionDto>> {
        const scheduleTransactions = await this.scheduleTransactionRepo.getAll()

        const response: GetAllScheduleTransactionDto[] = []
        scheduleTransactions.forEach(trans => {
            response.push({
                id: trans.getId(),
                name: trans.getName(),
                accountId: trans.getAccountRef(),
                categoryId: trans.getCategoryRef(),
                tagIds: trans.getTags(),
                type: trans.getTransactionType(),
                isPause: trans.getIsPause(),
                dateStart: trans.getSchedule().getStartedDate().toString(),
                dateUpdate: trans.getSchedule().getUpdatedDate().toString(),
                dateEnd: trans.getSchedule().getEndingDate()?.toString(),
                period: trans.getSchedule().getPeriod(),
                periodTime: trans.getSchedule().getPeriodTime()
            })
        })

        return { items: response, totals: response.length }
    }

}
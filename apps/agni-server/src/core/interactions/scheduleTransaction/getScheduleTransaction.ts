import { ScheduleTransactionRepository } from "@core/repositories/scheduleTransactionRepository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";

export type GetScheduleTransactionDto = {
    id: string
    name: string
    accountId: string
    categoryId: string
    tagIds: string[]
    type: string
    amount: number
    isPause: boolean
    dateStart: string
    period: string
    dateUpdate: string
    periodTime?: number
    dateEnd?: string
}

export class GetScheduleTransactionUsecase implements IUsecase<string, GetScheduleTransactionDto> {
    private scheduleTransactionRepo: ScheduleTransactionRepository 

    constructor(scheduleTransactionRepo: ScheduleTransactionRepository ) {
        this.scheduleTransactionRepo = scheduleTransactionRepo
    }

    async execute(id: string): Promise<GetScheduleTransactionDto> {
        const scheduleTransaction = await this.scheduleTransactionRepo.get(id)

        if (scheduleTransaction === null)
            throw new ResourceNotFoundError("SCHEDULE_TRANSACTION_NOT_FOUND")

        return {
            id: scheduleTransaction.getId(),
            name: scheduleTransaction.getName(),
            accountId: scheduleTransaction.getAccountRef(),
            categoryId: scheduleTransaction.getCategoryRef(),
            tagIds: scheduleTransaction.getTags(),
            amount: scheduleTransaction.getAmount().getAmount(),
            type: scheduleTransaction.getTransactionType(),
            isPause: scheduleTransaction.getIsPause(),
            dateStart: scheduleTransaction.getSchedule().getStartedDate().toLocaleString(),
            dateUpdate: scheduleTransaction.getSchedule().getUpdatedDate().toLocaleString(),
            dateEnd: scheduleTransaction.getSchedule().getEndingDate()?.toLocaleString(),
            period: scheduleTransaction.getSchedule().getPeriod(),
            periodTime: scheduleTransaction.getSchedule().getPeriodTime()
        }
    }
}
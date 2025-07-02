import { ScheduleTransactionRepository } from "@core/repositories/scheduleTransactionRepository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";

export type GetScheduleTransactionDto = {
    id: string
    name: string
    accountRef: string
    categoryRef: string
    tagRefs: string[]
    type: string
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
            accountRef: scheduleTransaction.getAccountRef(),
            categoryRef: scheduleTransaction.getCategoryRef(),
            tagRefs: scheduleTransaction.getTags(),
            type: scheduleTransaction.getTransactionType(),
            isPause: scheduleTransaction.getIsPause(),
            dateStart: scheduleTransaction.getSchedule().getStartedDate().toString(),
            dateUpdate: scheduleTransaction.getSchedule().getUpdatedDate().toString(),
            dateEnd: scheduleTransaction.getSchedule().getEndingDate()?.toString(),
            period: scheduleTransaction.getSchedule().getPeriod(),
            periodTime: scheduleTransaction.getSchedule().getPeriodTime()
        }
    }
}
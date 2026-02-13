import Repository from "@core/adapters/repository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { ScheduleTransaction } from "@core/domains/entities/scheduleTransaction";

export type GetScheduleTransactionDto = {
    id: string
    name: string
    accountId: string
    categoryId: string
    tagIds: string[]
    type: string
    amount: number
    isPause: boolean
    isFreeze: boolean
    dueDate: Date,
    repeater?: {
        period: string
        interval: number
    }
}

export class GetScheduleTransactionUsecase implements IUsecase<string, GetScheduleTransactionDto> {
    private scheduleTransactionRepo: Repository<ScheduleTransaction> 

    constructor(scheduleTransactionRepo: Repository<ScheduleTransaction> ) {
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
            isFreeze: scheduleTransaction.getIsFreeze(),
            dueDate: scheduleTransaction.getSchedule().dueDate,
            repeater: scheduleTransaction.getSchedule(). repeater ? {
                period: scheduleTransaction.getSchedule().repeater!.period,
                interval: scheduleTransaction.getSchedule().repeater!.interval
            } : undefined
        }
    }
}
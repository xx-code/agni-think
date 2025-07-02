import { ScheduleTransactionRepository } from "@core/repositories/scheduleTransactionRepository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";

export class DeleteScheduleTransactionUseCase implements IUsecase<string, void> {
    private readonly scheduleTransactionRepo: ScheduleTransactionRepository

    constructor(scheduleTransactionRepo: ScheduleTransactionRepository) {
        this.scheduleTransactionRepo = scheduleTransactionRepo
    }

    async execute(id: string): Promise<void> {
        if (!await this.scheduleTransactionRepo.existById(id))
            throw new ResourceNotFoundError("SCHEDULE_TRANSACTION_NOT_FOUND")

        await this.scheduleTransactionRepo.delete(id)
    }
}
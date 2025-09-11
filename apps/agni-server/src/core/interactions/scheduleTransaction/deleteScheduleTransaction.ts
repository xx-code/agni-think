import Repository from "@core/adapters/repository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { ScheduleTransaction } from "@core/domains/entities/scheduleTransaction";

export class DeleteScheduleTransactionUseCase implements IUsecase<string, void> {
    private readonly scheduleTransactionRepo: Repository<ScheduleTransaction>

    constructor(scheduleTransactionRepo: Repository<ScheduleTransaction>) {
        this.scheduleTransactionRepo = scheduleTransactionRepo
    }

    async execute(id: string): Promise<void> {
        if (!await this.scheduleTransactionRepo.get(id))
            throw new ResourceNotFoundError("SCHEDULE_TRANSACTION_NOT_FOUND")

        await this.scheduleTransactionRepo.delete(id)
    }
}
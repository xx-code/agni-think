import { CreatedDto } from "@core/dto/base";
import { IUsecase } from "../interfaces";
import { ScheduleTransactionRepository } from "@core/repositories/scheduleTransactionRepository";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { TransactionDependencies } from "../facades";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { ScheduleTransaction } from "@core/domains/entities/scheduleTransaction";
import { GetUID } from "@core/adapters/libs";
import { FREEZE_CATEGORY_ID, mapperMainTransactionCategory, mapperPeriod, TransactionType } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import ValidationError from "@core/errors/validationError";
import { Scheduler } from "@core/domains/valueObjects/scheduleInfo";

export type RequestCreateScheduleTransactionScheduler = {
    period: string,
    periodTime?: number
    dateStart: Date
    dateEnd?: Date
}

export type RequestCreateScheduleTransaction = {
    name: string
    accountId: string
    amount: number
    isFreeze: boolean
    categoryId: string
    description: string 
    tagIds: string[]
    type: string
    schedule: RequestCreateScheduleTransactionScheduler
}

export class CreateScheduleTransactionUseCase implements IUsecase<RequestCreateScheduleTransaction, CreatedDto> {
    private scheduleTransactionRepo: ScheduleTransactionRepository
    private transcationDependencies: TransactionDependencies

    constructor(
        transcationDependencies: TransactionDependencies, 
        scheduleTranscationRepo: ScheduleTransactionRepository) {
        this.transcationDependencies = transcationDependencies
        this.scheduleTransactionRepo = scheduleTranscationRepo
    }

    async execute(request: RequestCreateScheduleTransaction): Promise<CreatedDto> {
        if (await this.scheduleTransactionRepo.existByName(request.name))
            throw new ResourceAlreadyExist("SCHEDULE_TRANSACTION_ALREADY_EXIST") 

        if (!await this.transcationDependencies.accountRepository?.isExistById(request.accountId))
            throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

        if (!request.isFreeze && !await this.transcationDependencies.categoryRepository?.isCategoryExistById(request.categoryId))
            throw new ResourceNotFoundError("CATEGORY_NOT_FOUND")

        if (!request.isFreeze && request.tagIds.length > 0)
            if (!await this.transcationDependencies.tagRepository?.isTagExistByIds(request.tagIds))
                throw new ResourceNotFoundError("TAGS_NOT_FOUND")
        
        if (request.amount <= 0)
            throw new ValidationError("AMOUNT_SCHEDULE_TRANSACTION_MUST_GREATER_THAN_0")

        const scheduler = new Scheduler(
            mapperPeriod(request.schedule.period),
            request.schedule.dateStart ,
            request.schedule.periodTime,
            request.schedule.dateEnd
        )

        const scheduleTransaction = new ScheduleTransaction(
            GetUID(), 
            request.name, 
            request.accountId, 
            request.isFreeze ? FREEZE_CATEGORY_ID : request.categoryId,
            new Money(request.amount),
            request.isFreeze ? TransactionType.OTHER : mapperMainTransactionCategory(request.type),
            scheduler,
            false,
            false,
            request.isFreeze,
            request.tagIds
        )

        await this.scheduleTransactionRepo.create(scheduleTransaction)

        return { newId: scheduleTransaction.getId()}
    }
}
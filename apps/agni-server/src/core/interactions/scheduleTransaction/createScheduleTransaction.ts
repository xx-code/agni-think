import { CreatedDto } from "@core/dto/base";
import { IUsecase } from "../interfaces";
import { ScheduleTransactionRepository } from "@core/repositories/scheduleTransactionRepository";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { TransactionDependencies } from "../facades";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { ScheduleTransaction } from "@core/domains/entities/scheduleTransaction";
import { GetUID } from "@core/adapters/libs";
import { mapperMainTransactionCategory, mapperPeriod } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import ValidationError from "@core/errors/validationError";
import { Scheduler } from "@core/domains/valueObjects/scheduleInfo";
import { MomentDateService } from "@core/domains/entities/libs";

export type RequestCreateScheduleTransactionScheduler = {
    period: string,
    periodTime?: number
    dateStart: string
    dateEnd?: string
}

export type RequestCreateScheduleTransaction = {
    name: string
    accountRef: string
    amount: number
    categoryRef: string
    description: string 
    tagRefs: string[]
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

        if (!await this.transcationDependencies.accountRepository?.isExistById(request.accountRef))
            throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

        if (!await this.transcationDependencies.categoryRepository?.isCategoryExistById(request.categoryRef))
            throw new ResourceNotFoundError("CATEGORY_NOT_FOUND")

        if (request.tagRefs.length > 0)
            if (!await this.transcationDependencies.tagRepository?.isTagExistByIds(request.tagRefs))
                throw new ResourceNotFoundError("TAGS_NOT_FOUND")
        
        if (request.amount <= 0)
            throw new ValidationError("AMOUNT_SCHEDULE_TRANSACTION_MUST_GREATER_THAN_0")

        const scheduler = new Scheduler(
            mapperPeriod(request.schedule.period),
            MomentDateService.formatDate(request.schedule.dateStart) ,
            request.schedule.periodTime,
            request.schedule.dateEnd ? MomentDateService.formatDate(request.schedule.dateEnd) : undefined
        )

        const scheduleTransaction = new ScheduleTransaction(
            GetUID(), 
            request.name, 
            request.accountRef, 
            request.categoryRef,
            new Money(request.amount),
            mapperMainTransactionCategory(request.type),
            scheduler,
            false,
            request.tagRefs
        )

        await this.scheduleTransactionRepo.create(scheduleTransaction)

        return { newId: scheduleTransaction.getId()}
    }
}
import { CreatedDto } from "@core/dto/base";
import { IUsecase } from "../interfaces";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { TransactionDependencies } from "../facades";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { ScheduleTransaction } from "@core/domains/entities/scheduleTransaction";
import { GetUID } from "@core/adapters/libs";
import { FREEZE_CATEGORY_ID, mapperMainTransactionCategory, mapperPeriod, TransactionType } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import ValidationError from "@core/errors/validationError";
import { Scheduler } from "@core/domains/valueObjects/scheduleInfo";
import Repository from "@core/adapters/repository";

export type RequestCreateScheduleTransactionScheduler = {
    repeater?: {
        period: string
        interval: number
    }
    dueDate: Date
}

export type RequestCreateScheduleTransaction = {
    name: string
    accountId: string
    amount: number
    isFreeze: boolean
    categoryId?: string
    description: string 
    tagIds: string[]
    type?: string
    schedule: RequestCreateScheduleTransactionScheduler
}

export class CreateScheduleTransactionUseCase implements IUsecase<RequestCreateScheduleTransaction, CreatedDto> {
    private scheduleTransactionRepo: Repository<ScheduleTransaction>
    private transcationDependencies: TransactionDependencies

    constructor(
        transcationDependencies: TransactionDependencies, 
        scheduleTranscationRepo: Repository<ScheduleTransaction>) {
        this.transcationDependencies = transcationDependencies
        this.scheduleTransactionRepo = scheduleTranscationRepo
    }

    async execute(request: RequestCreateScheduleTransaction): Promise<CreatedDto> {
        if (await this.scheduleTransactionRepo.existByName(request.name))
            throw new ResourceAlreadyExist("SCHEDULE_TRANSACTION_ALREADY_EXIST") 

        if (!await this.transcationDependencies.accountRepository?.get(request.accountId))
            throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

        if (!request.isFreeze && !request.categoryId)
            throw new ValidationError("CATEGORY_MUST_BE_DEFINE_WHEN_IS_NOT_FREEZE_TRANSACTION")

        if (!request.isFreeze && !request.type)
            throw new ValidationError("TYPE_TRANSACTION_MUST_BE_DEFINE")

        if (!request.isFreeze && !await this.transcationDependencies.categoryRepository?.get(request.categoryId!))
            throw new ResourceNotFoundError("CATEGORY_NOT_FOUND")

        if (!request.isFreeze && request.tagIds.length > 0)
            if ((await this.transcationDependencies.tagRepository?.getManyByIds(request.tagIds))?.length === 0)
                throw new ResourceNotFoundError("TAGS_NOT_FOUND")
        
        if (request.amount <= 0)
            throw new ValidationError("AMOUNT_SCHEDULE_TRANSACTION_MUST_GREATER_THAN_0")

        const scheduler = new Scheduler(
            new Date(request.schedule.dueDate),
            request.schedule.repeater ? {
                period: mapperPeriod(request.schedule.repeater.period),
                interval: request.schedule.repeater.interval
            } : undefined
        )

        const scheduleTransaction = new ScheduleTransaction(
            GetUID(), 
            request.name, 
            request.accountId, 
            request.isFreeze ? FREEZE_CATEGORY_ID : request.categoryId!,
            new Money(request.amount),
            request.isFreeze ? TransactionType.OTHER : mapperMainTransactionCategory(request.type!),
            scheduler,
            false,
            request.isFreeze,
            request.isFreeze ? [] : request.tagIds
        )

        await this.scheduleTransactionRepo.create(scheduleTransaction)

        return { newId: scheduleTransaction.getId()}
    }
}
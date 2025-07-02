import { ScheduleTransactionRepository } from "@core/repositories/scheduleTransactionRepository"
import { IUsecase } from "../interfaces"
import { TransactionDependencies } from "../facades"
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError"
import { mapperMainTransactionCategory, mapperPeriod } from "@core/domains/constants"
import { Scheduler } from "@core/domains/valueObjects/scheduleInfo"
import { MomentDateService } from "@core/domains/entities/libs"
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError"
import { isStringDifferent } from "@core/domains/helpers"
import ValidationError from "@core/errors/validationError"
import { Money } from "@core/domains/entities/money"

export type RequestUpdateScheduleTransactionScheduler = {
    period: string,
    periodTime?: number
    dateStart: string
    dateEnd?: string
}

export type RequestUpdateScheduleTransaction = {
    id: string
    name: string
    accountRef: string
    amount: number
    categoryRef: string
    tagRefs: string[]
    type: string
    isPause: boolean
    schedule: RequestUpdateScheduleTransactionScheduler
}

export class UpdateScheduleTransactionUseCase implements IUsecase<RequestUpdateScheduleTransaction, void> {
    private scheduleTransactionRepo: ScheduleTransactionRepository
    private transactionDependencies: TransactionDependencies

    constructor(
        scheduleTransactionRepo: ScheduleTransactionRepository,
        transactionDependencies: TransactionDependencies) 
    {
        this.scheduleTransactionRepo = scheduleTransactionRepo
        this.transactionDependencies = transactionDependencies
    }

    async execute(request: RequestUpdateScheduleTransaction): Promise<void> {
        const scheduleTransaction = await this.scheduleTransactionRepo.get(request.id)
        if (scheduleTransaction === null)
            throw new ResourceNotFoundError("SCHEDULE_TRANSACTION_NOT_FOUND")

        if (await this.scheduleTransactionRepo.existByName(request.name) && !isStringDifferent(request.name, scheduleTransaction.getName()))
            throw new ResourceAlreadyExist("SCHEDULE_TRANSACTION_ALREADY_EXIST") 

        if (!await this.transactionDependencies.accountRepository?.isExistById(request.accountRef))
            throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

        if (!await this.transactionDependencies.categoryRepository?.isCategoryExistById(request.categoryRef))
            throw new ResourceNotFoundError("CATEGORY_NOT_FOUND")

        if (request.tagRefs.length > 0)
            if (!await this.transactionDependencies.tagRepository?.isTagExistByIds(request.tagRefs))
                throw new ResourceNotFoundError("TAGS_NOT_FOUND")
        
        if (request.amount <= 0)
            throw new ValidationError("AMOUNT_SCHEDULE_TRANSACTION_MUST_GREATER_THAN_0")

        const scheduler = new Scheduler(
            mapperPeriod(request.schedule.period),
            MomentDateService.formatDate(request.schedule.dateStart) ,
            request.schedule.periodTime,
            request.schedule.dateEnd ? MomentDateService.formatDate(request.schedule.dateEnd) : undefined
        )

        scheduleTransaction.setAccountRef(request.accountRef)
        scheduleTransaction.setCategoryRef(request.categoryRef)
        scheduleTransaction.setTags(request.tagRefs)
        scheduleTransaction.setTransactionType(mapperMainTransactionCategory(request.type))
        scheduleTransaction.setName(request.name)
        scheduleTransaction.setAmount(new Money(request.amount))
        scheduleTransaction.setIsPause(request.isPause)
        scheduleTransaction.reSchedule(scheduler)

        await this.scheduleTransactionRepo.update(scheduleTransaction)
    }

}
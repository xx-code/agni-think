import { ScheduleTransactionRepository } from "@core/repositories/scheduleTransactionRepository"
import { IUsecase } from "../interfaces"
import { TransactionDependencies } from "../facades"
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError"
import { mapperMainTransactionCategory, mapperPeriod } from "@core/domains/constants"
import { Scheduler } from "@core/domains/valueObjects/scheduleInfo"
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError"
import { isStringDifferent } from "@core/domains/helpers"
import ValidationError from "@core/errors/validationError"
import { Money } from "@core/domains/entities/money"

export type RequestUpdateScheduleTransactionScheduler = {
    period: string,
    periodTime?: number
    dateStart: Date
    dateEnd?: Date
}

export type RequestUpdateScheduleTransaction = {
    id: string
    name?: string
    accountId?: string
    amount?: number
    categoryId?: string
    tagIds?: string[]
    type?: string
    isPause?: boolean
    schedule?: RequestUpdateScheduleTransactionScheduler
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

        if (request.name) {
            if (await this.scheduleTransactionRepo.existByName(request.name) && isStringDifferent(request.name, scheduleTransaction.getName()))
                throw new ResourceAlreadyExist("SCHEDULE_TRANSACTION_ALREADY_EXIST")

            scheduleTransaction.setName(request.name)
        }

        if (request.accountId) {
            if (!await this.transactionDependencies.accountRepository?.isExistById(request.accountId))
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")
            
            scheduleTransaction.setAccountRef(request.accountId)
        }

        if (!scheduleTransaction.getIsFreeze() && request.categoryId) {
            if (!await this.transactionDependencies.categoryRepository?.isCategoryExistById(request.categoryId))
                throw new ResourceNotFoundError("CATEGORY_NOT_FOUND")

            scheduleTransaction.setCategoryRef(request.categoryId)
        }

        if (!scheduleTransaction.getIsFreeze() && request.tagIds) {
            if (request.tagIds.length > 0)
                if (!await this.transactionDependencies.tagRepository?.isTagExistByIds(request.tagIds))
                    throw new ResourceNotFoundError("TAGS_NOT_FOUND")

            scheduleTransaction.setTags(request.tagIds)
        }

        if (!scheduleTransaction.getIsFreeze() && request.type) {
            scheduleTransaction.setTransactionType(mapperMainTransactionCategory(request.type))
        }

        if (request.amount) {
            if (request.amount <= 0)
                throw new ValidationError("AMOUNT_SCHEDULE_TRANSACTION_MUST_GREATER_THAN_0")

            scheduleTransaction.setAmount(new Money(request.amount))
        }

        if (request.schedule) {
            const scheduler = new Scheduler(
                mapperPeriod(request.schedule.period),
                request.schedule.dateStart ,
                request.schedule.periodTime,
                request.schedule.dateEnd
            )

            scheduleTransaction.reSchedule(scheduler)
        }

        if (request.isPause !== undefined) {
            scheduleTransaction.setIsPause(request.isPause)
        } 

        await this.scheduleTransactionRepo.update(scheduleTransaction)
    }

}
import { IUsecase } from "../interfaces"
import { TransactionDependencies } from "../facades"
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError"
import { mapperMainTransactionCategory, mapperPeriod } from "@core/domains/constants"
import { Scheduler } from "@core/domains/valueObjects/scheduleInfo"
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError"
import { isStringDifferent } from "@core/domains/helpers"
import ValidationError from "@core/errors/validationError"
import { Money } from "@core/domains/entities/money"
import Repository from "@core/adapters/repository"
import { ScheduleTransaction } from "@core/domains/entities/scheduleTransaction"

export type RequestUpdateScheduleTransactionScheduler = {
    repeater?: {
        period: string
        interval: number
    }
    dueDate: Date
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
    private scheduleTransactionRepo: Repository<ScheduleTransaction>
    private transactionDependencies: TransactionDependencies

    constructor(
        scheduleTransactionRepo: Repository<ScheduleTransaction>,
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
            if (!await this.transactionDependencies.accountRepository?.get(request.accountId))
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")
            
            scheduleTransaction.setAccountRef(request.accountId)
        }

        if (!scheduleTransaction.getIsFreeze() && request.categoryId) {
            if (!await this.transactionDependencies.categoryRepository?.get(request.categoryId))
                throw new ResourceNotFoundError("CATEGORY_NOT_FOUND")

            scheduleTransaction.setCategoryRef(request.categoryId)
        }

        if (!scheduleTransaction.getIsFreeze() && request.tagIds) {
            if (request.tagIds.length > 0)
                if ((await this.transactionDependencies.tagRepository?.getManyByIds(request.tagIds))?.length === 0)
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
            new Date(request.schedule.dueDate),
            request.schedule.repeater ? {
                period: mapperPeriod(request.schedule.repeater.period),
                interval: request.schedule.repeater.interval
            } : undefined
        )

            scheduleTransaction.reSchedule(scheduler)
        }

        if (request.isPause !== undefined) {
            scheduleTransaction.setIsPause(request.isPause)
        } 

        if (scheduleTransaction.hasChange())
            await this.scheduleTransactionRepo.update(scheduleTransaction)
    }

}
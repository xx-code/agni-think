import { GetUID } from "@core/adapters/libs"
import { mapperMainTransactionCategory, mapperRecordType, mapperTransactionStatus, RecordType, TransactionStatus, TransactionType } from "@core/domains/constants"
import { Money } from "@core/domains/entities/money"
import { Record } from "@core/domains/entities/record"
import { Transaction } from "@core/domains/entities/transaction"
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError"
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository"
import { ValueError } from "@core/errors/valueError"
import { TransactionDependencies } from "../facades"
import { CreatedDto } from "@core/dto/base"
import { IUsecase } from "../interfaces"
import Repository from "@core/adapters/repository"
import UnExpectedError from "@core/errors/unExpectedError"
import { TransactionDeduction } from "@core/domains/valueObjects/transactionDeduction"

export type RequestAddTransactionUseCase = {
    accountId: string
    status: string
    date: Date
    type: string
    mouvement: string
    currencyId?: string
    records: {
        amount: number
        categoryId: string
        description: string
        tagIds: string[]
        budgetIds: string[]
    }[]
    deductions: {
        deductionId: string
        amount: number
    }[]
}


export class AddTransactionUseCase implements IUsecase<RequestAddTransactionUseCase, CreatedDto> {
    private transactionRepository: Repository<Transaction>
    private transcationDependencies: TransactionDependencies
    private unitOfWork: UnitOfWorkRepository

    constructor(
        unitOfWork: UnitOfWorkRepository,
        transactionRepo: Repository<Transaction>,
        transactionDependencies: TransactionDependencies
    ) {
        this.transactionRepository = transactionRepo
        this.transcationDependencies = transactionDependencies
        this.unitOfWork = unitOfWork
    }

    async execute(request: RequestAddTransactionUseCase, trx?: any): Promise<CreatedDto> {
        try {
            let innerTrx = trx
            if (!trx)
                innerTrx = await this.unitOfWork.start() 

            let account = await this.transcationDependencies.accountRepository?.get(request.accountId) 
            if (account === null)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")
        
            const type = mapperMainTransactionCategory(request.type) 
            
            const status = mapperTransactionStatus(request.status)

            if (request.deductions.length > 0) {
                if (!request.deductions.every(i => i.amount >= 0))
                    throw new ResourceNotFoundError("DEDUCTION_MUST_NOT_BE_NEGATIVE")

                const foundDeductions = await this.transcationDependencies.deductionRepository?.getManyByIds(request.deductions.map(i => i.deductionId)) ?? [];
                if (request.deductions.length !== foundDeductions.length)
                    throw new ResourceNotFoundError("DEDUCTION_NOT_FOUND");
            }

            const recordType = mapperRecordType(request.mouvement)
            let newTransaction = new Transaction(
                GetUID(), 
                request.accountId, 
                request.date,
                type, 
                recordType,
                status
            )
            newTransaction.setDeductions(request.deductions.map(i => new TransactionDeduction(newTransaction.getId(), i.deductionId, i.amount)))
            await this.transactionRepository.create(newTransaction, innerTrx);

            if (request.records.length  === 0)
                throw new UnExpectedError("YOU_MUST_HAVE_RECORDS_FOR_TRANSACTION")

            if (recordType === RecordType.CREDIT && (type !== TransactionType.INCOME && type !== TransactionType.OTHER))
                throw new UnExpectedError("A CREDIT transaction must be Income or Other")

            if (recordType === RecordType.DEBIT && type === TransactionType.INCOME)
                throw new UnExpectedError("A DEBIT transaction Cant be Income")

            let totalAmount = 0
            for (let i = 0; i < request.records.length; i++) {
                const record = request.records[i]

                if (!(await this.transcationDependencies.categoryRepository?.get(record.categoryId)))
                    throw new ResourceNotFoundError("CATEGORY_NOT_FOUND")

                if (record.tagIds.length > 0) {
                    const foundTags = await this.transcationDependencies.tagRepository?.getManyByIds(record.tagIds) ?? [];
                    if (foundTags.length !== record.tagIds.length)
                        throw new ResourceNotFoundError("TAGS_NOT_FOUND");
                }

                if (record.budgetIds.length > 0) {
                    const foundBudgets = await this.transcationDependencies.budgetRepository?.getManyByIds(record.budgetIds) ?? [];
                    if (foundBudgets.length !== record.budgetIds.length)
                        throw new ResourceNotFoundError("BUDGETS_NOT_FOUND");
                } 

                if (record.amount <= 0)
                    throw new ValueError("AMOUNT_MUST_GREATER_THANT_0")


                let amount = new Money(record.amount)
                totalAmount += record.amount
                const newRecord = new Record(
                    GetUID(),
                    newTransaction.getId(),
                    amount,
                    record.categoryId,
                    record.description,
                    record.tagIds,
                    record.budgetIds
                )
                await this.transcationDependencies.recordRepository?.create(newRecord, innerTrx)
            } 

            if (status === TransactionStatus.COMPLETE) {

                if (recordType === RecordType.CREDIT)
                    account!.addOnBalance(new Money(totalAmount)) 
                else
                    account!.substractBalance(new Money(totalAmount))

                await this.transcationDependencies.accountRepository?.update(account!, innerTrx)
            }     
            
            if (!trx)
                await this.unitOfWork.commit()

            return {newId: newTransaction.getId()}
        } catch (err) {
            console.log(err)
            if (!trx)
                await this.unitOfWork.rollback()
            throw err
        }
    }
}
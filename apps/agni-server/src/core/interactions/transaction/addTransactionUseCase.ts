import { TransactionRepository } from "../../repositories/transactionRepository"
import { GetUID } from "@core/adapters/libs"
import { mapperMainTransactionCategory, RecordType, TransactionStatus, TransactionType } from "@core/domains/constants"
import { Money } from "@core/domains/entities/money"
import { Record } from "@core/domains/entities/record"
import { Transaction } from "@core/domains/entities/transaction"
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError"
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository"
import { ValueError } from "@core/errors/valueError"
import { TransactionDependencies } from "../facades"
import { MomentDateService } from "@core/domains/entities/libs"
import { CreatedDto } from "@core/dto/base"
import { IUsecase } from "../interfaces"

export type RequestAddTransactionUseCase = {
    accountId: string
    amount: number
    categoryId: string
    description: string
    date: string
    tagIds: string[]
    budgetIds: string[]
    type: string
}


export class AddTransactionUseCase implements IUsecase<RequestAddTransactionUseCase, CreatedDto> {
    private transactionRepository: TransactionRepository
    private transcationDependencies: TransactionDependencies
    private unitOfWork: UnitOfWorkRepository

    constructor(
        unitOfWork: UnitOfWorkRepository,
        transactionRepo: TransactionRepository,
        transactionDependencies: TransactionDependencies
    ) {
        this.transactionRepository = transactionRepo
        this.transcationDependencies = transactionDependencies
        this.unitOfWork = unitOfWork
    }

    async execute(request: RequestAddTransactionUseCase): Promise<CreatedDto> {
        try {
            await this.unitOfWork.start()

            let account = await this.transcationDependencies.accountRepository?.get(request.accountId) 
            if (account === null)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

            if (!(await this.transcationDependencies.categoryRepository?.isCategoryExistById(request.categoryId)))
                throw new ResourceNotFoundError("CATEGORY_NOT_FOUND")

            if (request.tagIds.length > 0 && !(await this.transcationDependencies.tagRepository?.isTagExistByIds(request.tagIds)))
                throw new ResourceNotFoundError("TAGS_NOT_FOUND")

            if (request.budgetIds.length > 0 && !(await this.transcationDependencies.budgetRepository?.isBudgetExistByIds(request.budgetIds)))
                throw new ResourceNotFoundError("BUDGETS_NOT_FOUND")
           
            if (request.amount <= 0)
                throw new ValueError("AMOUNT_MUST_GREATER_THANT_0")

            let amount = new Money(request.amount)

            let date = MomentDateService.formatDateWithtime(request.date)

            const type = mapperMainTransactionCategory(request.type)

            let newRecord = new Record(
                GetUID(), 
                amount, 
                date.toString(), 
                type === TransactionType.INCOME ? RecordType.CREDIT : RecordType.DEBIT, 
                request.description)
            await this.transcationDependencies.recordRepository?.save(newRecord)

            newRecord.getType() === RecordType.CREDIT ? account!.addOnBalance(amount) : account!.substractBalance(amount)

            await this.transcationDependencies.accountRepository?.update(account!)
            
            let newTransaction = new Transaction(
                GetUID(), 
                request.accountId, 
                newRecord.getId(), 
                request.categoryId, 
                date.toString(), type, TransactionStatus.COMPLETE, 
                request.tagIds, request.budgetIds)    

            await this.transactionRepository.save(newTransaction);
            
            await this.unitOfWork.commit()

            return {newId: newTransaction.getId()}
        } catch (err) {
            await this.unitOfWork.rollback()
            throw err
        }
    }
}
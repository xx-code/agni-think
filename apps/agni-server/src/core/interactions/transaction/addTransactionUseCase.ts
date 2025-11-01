import { GetUID } from "@core/adapters/libs"
import { mapperMainTransactionCategory, RecordType, TransactionStatus, TransactionType } from "@core/domains/constants"
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

export type RequestAddTransactionUseCase = {
    accountId: string
    amount: number
    categoryId: string
    description: string
    date: Date
    tagIds: string[]
    budgetIds: string[]
    type: string
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

    async execute(request: RequestAddTransactionUseCase): Promise<CreatedDto> {
        try {
            // await this.unitOfWork.start()

            let account = await this.transcationDependencies.accountRepository?.get(request.accountId) 
            if (account === null)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

            if (!(await this.transcationDependencies.categoryRepository?.get(request.categoryId)))
                throw new ResourceNotFoundError("CATEGORY_NOT_FOUND")

            if (request.tagIds.length > 0) {
                const foundTags = await this.transcationDependencies.tagRepository?.getManyByIds(request.tagIds) ?? [];
                if (foundTags.length !== request.tagIds.length)
                    throw new ResourceNotFoundError("TAGS_NOT_FOUND");
            }

            if (request.budgetIds.length > 0) {
                const foundBudgets = await this.transcationDependencies.budgetRepository?.getManyByIds(request.budgetIds) ?? [];
                if (foundBudgets.length !== request.budgetIds.length)
                    throw new ResourceNotFoundError("BUDGETS_NOT_FOUND");
            }
           
            if (request.amount <= 0)
                throw new ValueError("AMOUNT_MUST_GREATER_THANT_0")

            let amount = new Money(request.amount)

            const type = mapperMainTransactionCategory(request.type)

            let newRecord = new Record(
                GetUID(), 
                amount, 
                request.date, 
                type === TransactionType.INCOME ? RecordType.CREDIT : RecordType.DEBIT, 
                request.description)
            await this.transcationDependencies.recordRepository?.create(newRecord)

            newRecord.getType() === RecordType.CREDIT ? account!.addOnBalance(amount) : account!.substractBalance(amount)

            await this.transcationDependencies.accountRepository?.update(account!)
            
            let newTransaction = new Transaction(
                GetUID(), 
                request.accountId, 
                newRecord.getId(), 
                request.categoryId, 
                request.date, type, TransactionStatus.COMPLETE, 
                request.tagIds, request.budgetIds)    

            await this.transactionRepository.create(newTransaction);
            
            // await this.unitOfWork.commit()

            return {newId: newTransaction.getId()}
        } catch (err) {
            // await this.unitOfWork.rollback()
            return {newId: ''}
        }
    }
}
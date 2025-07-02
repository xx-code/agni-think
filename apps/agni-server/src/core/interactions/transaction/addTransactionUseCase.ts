import { TransactionRepository } from "../../repositories/transactionRepository"
import { AccountRepository } from "../../repositories/accountRepository"
import { CategoryRepository } from "../../repositories/categoryRepository"
import { TagRepository } from "../../repositories/tagRepository"
import { RecordRepository } from "../../repositories/recordRepository"
import { DateService, GetUID } from "@core/adapters/libs"
import { mapperMainTransactionCategory, mapperTransactionType, RecordType, TransactionStatus, TransactionType } from "@core/domains/constants"
import { Money } from "@core/domains/entities/money"
import { Record } from "@core/domains/entities/record"
import { Transaction } from "@core/domains/entities/transaction"
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError"
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository"
import { ValueError } from "@core/errors/valueError"
import { BudgetRepository } from "@core/repositories/budgetRepository"
import { TransactionDependencies } from "../facades"
import { MomentDateService } from "@core/domains/entities/libs"
import { CreatedDto } from "@core/dto/base"
import { IUsecase } from "../interfaces"

export type RequestAddTransactionUseCase = {
    accountRef: string
    amount: number
    categoryRef: string
    description: string
    date: string
    tagRefs: string[]
    budgetRefs: string[]
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

            let account = await this.transcationDependencies.accountRepository?.get(request.accountRef) 
            if (account === null)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

            if (!(await this.transcationDependencies.categoryRepository?.isCategoryExistById(request.categoryRef)))
                throw new ResourceNotFoundError("CATEGORY_NOT_FOUND")

            if (request.tagRefs.length > 0 && !(await this.transcationDependencies.tagRepository?.isTagExistByIds(request.tagRefs)))
                throw new ResourceNotFoundError("TAGS_NOT_FOUND")

            if (request.budgetRefs.length > 0 && !(await this.transcationDependencies.budgetRepository?.isBudgetExistByIds(request.budgetRefs)))
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
                request.accountRef, 
                newRecord.getId(), 
                request.categoryRef, 
                date.toString(), type, TransactionStatus.COMPLETE, 
                request.tagRefs, request.budgetRefs)    

            await this.transactionRepository.save(newTransaction);
            
            await this.unitOfWork.commit()

            return {newId: newTransaction.getId()}
        } catch (err) {
            await this.unitOfWork.rollback()
            throw err
        }
    }
}
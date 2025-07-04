import { TransactionRepository } from "../../repositories/transactionRepository"
import { AccountRepository } from "../../repositories/accountRepository"
import { CategoryRepository } from "../../repositories/categoryRepository"
import { TagRepository } from "../../repositories/tagRepository"
import { RecordRepository } from "../../repositories/recordRepository"
import { DateService, GetUID } from "@core/adapters/libs"
import { mapperMainTransactionCategory, mapperTransactionType, TransactionMainCategory } from "@core/domains/constants"
import { Money } from "@core/domains/entities/money"
import { Record, TransactionType } from "@core/domains/entities/record"
import { Transaction } from "@core/domains/entities/transaction"
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError"
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository"
import { ValueError } from "@core/errors/valueError"
import { BudgetRepository } from "@core/repositories/budgetRepository"

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

export interface IAddTransactionUseCase {
    execute(request: RequestAddTransactionUseCase): void;
}

export interface IAddTransactionUseCaseResponse {
    success(tranactionId: string): void;
    fail(err: Error): void
}

export interface IAddTransactionAdapter {
    transactionRepository: TransactionRepository
    budgetRepository: BudgetRepository
    recordRepository: RecordRepository
    categoryRepository: CategoryRepository
    tagRepository: TagRepository
    unitOfWork: UnitOfWorkRepository
    dateService: DateService
    accountRepository: AccountRepository
}

export class AddTransactionUseCase implements IAddTransactionUseCase {
    private transactionRepository: TransactionRepository;
    private recordRepository: RecordRepository;
    private categoryRepository: CategoryRepository;
    private budgetRepository: BudgetRepository;
    private tagRepository: TagRepository;
    private accountRepository: AccountRepository;
    private unitOfWork: UnitOfWorkRepository
    private dateService: DateService
    private presenter: IAddTransactionUseCaseResponse

    constructor(adapters: IAddTransactionAdapter, presenter: IAddTransactionUseCaseResponse) {
        this.transactionRepository = adapters.transactionRepository
        this.budgetRepository = adapters.budgetRepository
        this.recordRepository = adapters.recordRepository
        this.categoryRepository = adapters.categoryRepository
        this.tagRepository = adapters.tagRepository
        this.accountRepository = adapters.accountRepository
        this.unitOfWork = adapters.unitOfWork
        this.dateService = adapters.dateService
        this.presenter = presenter
    }

    async execute(request: RequestAddTransactionUseCase): Promise<void> {
        try {
            await this.unitOfWork.start()

            let account = await this.accountRepository.get(request.accountRef) 

            if (!(await this.categoryRepository.isCategoryExistById(request.categoryRef)))
                throw new ResourceNotFoundError("Category not found")

            if (request.tagRefs.length > 0 && !(await this.tagRepository.isTagExistByIds(request.tagRefs)))
                throw new ResourceNotFoundError("A tag are not found")

            if (request.budgetRefs.length > 0 && !(await this.budgetRepository.isBudgetExistByIds(request.budgetRefs)))
                throw new ResourceNotFoundError("A Budget are not found")
           
            if (request.amount <= 0)
                throw new ValueError("You can 't add transaction less or equal to 0")

            let amount = new Money(request.amount)

            let date = this.dateService.formatDateWithtime(request.date)

            const type = mapperMainTransactionCategory(request.type)

            let newRecord = new Record(
                GetUID(), 
                amount, 
                date, 
                type === TransactionMainCategory.INCOME ? TransactionType.CREDIT : TransactionType.DEBIT, 
                request.description)
            await this.recordRepository.save(newRecord)

            newRecord.getType() === TransactionType.CREDIT ? account.addOnBalance(amount) : account.substractBalance(amount)

            await this.accountRepository.update(account)
            
            let newTransaction = new Transaction(GetUID(), request.accountRef, newRecord.getId(), request.categoryRef, 
            date, type, request.tagRefs, request.budgetRefs)    
            await this.transactionRepository.save(newTransaction);
            
            await this.unitOfWork.commit()
            this.presenter.success(newTransaction.getId())
        } catch (err) {
            await this.unitOfWork.rollback()
            this.presenter.fail(err as Error)
        }
    }
}
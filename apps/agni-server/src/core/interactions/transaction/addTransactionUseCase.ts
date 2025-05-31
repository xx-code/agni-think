import { TransactionRepository } from "../../repositories/transactionRepository"
import { AccountRepository } from "../../repositories/accountRepository"
import { CategoryRepository } from "../../repositories/categoryRepository"
import { TagRepository } from "../../repositories/tagRepository"
import { RecordRepository } from "../../repositories/recordRepository"
import { DateService, GetUID } from "@core/adapters/libs"
import { mapperMainTransactionCategory, mapperTransactionType } from "@core/domains/constants"
import { Money } from "@core/domains/entities/money"
import { Record, TransactionType } from "@core/domains/entities/record"
import { Transaction } from "@core/domains/entities/transaction"
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError"
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository"
import { ValueError } from "@core/errors/valueError"

export type RequestAddTransactionUseCase = {
    accountRef: string
    amount: number
    categoryRef: string
    description: string
    date: string
    tagRefs: string[]
    type: string
    mainCategory: string
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
    private tagRepository: TagRepository;
    private accountRepository: AccountRepository;
    private unitOfWork: UnitOfWorkRepository
    private dateService: DateService
    private presenter: IAddTransactionUseCaseResponse

    constructor(adapters: IAddTransactionAdapter, presenter: IAddTransactionUseCaseResponse) {
        this.transactionRepository = adapters.transactionRepository
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

            if (!(await this.tagRepository.isTagExistByIds(request.tagRefs)))
                throw new ResourceNotFoundError("A tag are not found")
           
            if (request.amount <= 0)
                throw new ValueError("You can 't add transaction less or equal to 0")

            let amount = new Money(request.amount)

            let date = this.dateService.formatDateWithtime(request.date)

            let newRecord = new Record(GetUID(), amount, date, mapperTransactionType(request.type))
            newRecord.setDescription(request.description)
            await this.recordRepository.save(newRecord)

            newRecord.getType() === TransactionType.CREDIT ? account.addOnBalance(amount) : account.substractBalance(amount)

            await this.accountRepository.update(account)

            
            const mainCat = mapperMainTransactionCategory(request.mainCategory)
            let newTransaction = new Transaction(GetUID(), request.accountRef, newRecord.getId(), request.categoryRef, date, mainCat, request.tagRefs)    
            await this.transactionRepository.save(newTransaction);
            
            await this.unitOfWork.commit()
            this.presenter.success(newTransaction.getId())
        } catch (err) {
            await this.unitOfWork.rollback()
            this.presenter.fail(err as Error)
        }
    }
}
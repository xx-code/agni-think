import { RecordRepository } from "../../repositories/recordRepository";
import { AccountRepository } from "../../repositories/accountRepository";
import { TagRepository } from "../../repositories/tagRepository";
import { CategoryRepository } from "../../repositories/categoryRepository";
import { TransactionRepository } from "../../repositories/transactionRepository";
import { DateService } from "@core/adapters/libs";
import { FREEZE_CATEGORY_ID, mapperMainTransactionCategory, mapperTransactionType, SAVING_CATEGORY_ID } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { AddTransactionUseCase, IAddTransactionUseCaseResponse } from "./addTransactionUseCase";
import { DeleteTransactionUseCase, IDeleteTransactoinUseCaseResponse } from "./deleteTransactionUseCase";
import { ValueError } from "@core/errors/valueError";
import { BudgetRepository } from "@core/repositories/budgetRepository";


export type RequestUpdateTransactionUseCase = {
    id: string;
    accountRef: string
    tagsRef: string[]
    categoryRef: string
    type: string
    description: string
    date: string
    amount: number
    mainCategory: string
}

export interface IUpdateTransactionUseCase {
    execute(request: RequestUpdateTransactionUseCase): void
}

export interface IUpdateTransactionUseCaseResponse {
    success(newTransationId: string): void
    fail(err: Error): void
}

export interface IUpdateTransactionAdapter {
    budgetRepository: BudgetRepository
    transactionRepository: TransactionRepository
    categoryRepository: CategoryRepository
    tagRepository: TagRepository
    recordRepository: RecordRepository
    accountRepository: AccountRepository
    dateService: DateService
    unitOfWork: UnitOfWorkRepository
}


export class UpdateTransactionUseCase implements IUpdateTransactionUseCase {
    private transactionRepository: TransactionRepository;
    private recordRepository: RecordRepository;
    private categoryRepository: CategoryRepository;
    private tagRepository: TagRepository;
    private accountRepository: AccountRepository;
    private budgetRepository: BudgetRepository;
    private unitOfWork: UnitOfWorkRepository;

    private dateService: DateService

    private presenter: IUpdateTransactionUseCaseResponse;

    private resultatAddTransaction: string = ""
    private addTransactionPresenter: IAddTransactionUseCaseResponse = {
        success: (newTransaction: string) => {
            this.resultatAddTransaction = newTransaction
        },
        fail: (err: Error) => {
            this.presenter.fail(err)
            this.unitOfWork.rollback()
        }
    }
    private deleteTransactionPresenter: IDeleteTransactoinUseCaseResponse = {
        success: (success: boolean) => {
          
        },
        fail: (err: Error) => {
            this.presenter.fail(err)
            this.unitOfWork.rollback()
        }
    }

    constructor(adapter: IUpdateTransactionAdapter, presenter: IUpdateTransactionUseCaseResponse,) {
        this.transactionRepository = adapter.transactionRepository;
        this.recordRepository = adapter.recordRepository;
        this.categoryRepository = adapter.categoryRepository;
        this.tagRepository = adapter.tagRepository;
        this.accountRepository = adapter.accountRepository;
        this.budgetRepository = adapter.budgetRepository;
        this.presenter = presenter;
        this.unitOfWork = adapter.unitOfWork
        this.dateService = adapter.dateService
    }

    async execute(request: RequestUpdateTransactionUseCase): Promise<void> {
        try {
            // TODO: comment workflow
            let transaction = await this.transactionRepository.get(request.id);

            if ([SAVING_CATEGORY_ID, FREEZE_CATEGORY_ID].includes(transaction.getCategoryRef()))
                throw new ValueError("You cannot update this type of transaction")

            let record = await this.recordRepository.get(transaction.getRecordRef())

            if (request.amount <= 0)
                throw new ValueError("You can 't add transaction less or equal to 0")

            let amount = new Money(request.amount)

            record.setMoney(amount)
            
            record.setDescription(request.description)

            record.setType(mapperTransactionType(request.type))
            let date = this.dateService.formatDateWithtime(request.date)
            record.setDate(date)

            if (!(await this.accountRepository.isExistById(request.accountRef)))
                throw new ResourceNotFoundError("Account not found")
            if (!(await this.categoryRepository.isCategoryExistById(request.categoryRef)))
                throw new ResourceNotFoundError("Category not found")

            transaction.setCategoryRef(request.categoryRef)

            transaction.setTransactionType(mapperMainTransactionCategory(request.mainCategory))

            if (!(await this.tagRepository.isTagExistByIds(request.tagsRef)))
                throw new ResourceNotFoundError("a tag not found")

            transaction.setTags(request.tagsRef)

            let newTransationId = request.id
            if (record.hasChange() || transaction.hasChange())  {
                await (new DeleteTransactionUseCase(this.transactionRepository, this.recordRepository, this.unitOfWork, 
                    this.accountRepository, this.deleteTransactionPresenter)).execute(request.id)
                
                await (new AddTransactionUseCase({
                    accountRepository: this.accountRepository,
                    categoryRepository: this.categoryRepository,
                    dateService: this.dateService,
                    recordRepository: this.recordRepository,
                    tagRepository: this.tagRepository,
                    transactionRepository: this.transactionRepository,
                    budgetRepository: this.budgetRepository,
                    unitOfWork: this.unitOfWork,
                }, this.addTransactionPresenter)).execute({
                    accountRef: transaction.getAccountRef(),
                    amount: record.getMoney().getAmount(),
                    categoryRef: transaction.getCategoryRef(),
                    tagRefs: transaction.getTags(),
                    date: record.getDate(),
                    description: record.getDescription(),
                    type: transaction.getTransactionType(),
                    budgetRefs: transaction.getBudgetRefs()
                })

                newTransationId = this.resultatAddTransaction
            }

            this.presenter.success(newTransationId)
        } catch (err) {
            this.presenter.fail(err as Error)
        }
    }
}
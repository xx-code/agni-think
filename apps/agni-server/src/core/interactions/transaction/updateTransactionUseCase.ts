import { FREEZE_CATEGORY_ID, mapperMainTransactionCategory, RecordType, SAVING_CATEGORY_ID, TransactionType } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { RequestAddTransactionUseCase } from "./addTransactionUseCase";
import { ValueError } from "@core/errors/valueError";
import { IUsecase } from "../interfaces";
import { TransactionDependencies } from "../facades";
import { CreatedDto } from "@core/dto/base";
import Repository from "@core/adapters/repository";
import { Transaction } from "@core/domains/entities/transaction";


export type RequestUpdateTransactionUseCase = {
    id: string;
    accountId?: string
    tagIds?: string[]
    budgetIds?: string[]
    categoryId?: string
    type?: string
    description?: string
    date?: Date
    amount?: number
}


export class UpdateTransactionUseCase implements IUsecase<RequestUpdateTransactionUseCase, void> {
    private transactionRepository: Repository<Transaction>
    private transationDependencies: TransactionDependencies 
    private unitOfWork: UnitOfWorkRepository

    private addTransactionUsecase: IUsecase<RequestAddTransactionUseCase, CreatedDto>
    private deleteTransactionUsecase: IUsecase<string, void>

    constructor(
        transactionRepository: Repository<Transaction>,
        transationDependencies: TransactionDependencies,
        addTransactionUsecase: IUsecase<RequestAddTransactionUseCase, CreatedDto>,
        deleteTransactionUsecase: IUsecase<string, void>,
        unitOfWork: UnitOfWorkRepository
    ) {
        this.transactionRepository = transactionRepository
        this.transationDependencies = transationDependencies
        this.addTransactionUsecase = addTransactionUsecase
        this.deleteTransactionUsecase = deleteTransactionUsecase
        this.unitOfWork = unitOfWork
    }

    async execute(request: RequestUpdateTransactionUseCase): Promise<void> {
        try {
            await this.unitOfWork.start()

            let transaction = await this.transactionRepository.get(request.id);
            if (!transaction)
                throw new ResourceNotFoundError("TRANSACTION_NOT_FOUND")

            if ([SAVING_CATEGORY_ID, FREEZE_CATEGORY_ID].includes(transaction.getCategoryRef()))
                throw new ValueError("CANT_UPDATE_TRANSACTION")

            if (request.accountId) {
                if (!await this.transationDependencies.accountRepository?.get(request.accountId))
                    throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")
                transaction.setAccountRef(request.accountId)
            }

            let record = await this.transationDependencies.recordRepository?.get(transaction.getRecordRef())
            if (!record)
                throw new ResourceNotFoundError("RECORD_NOT_FOUND")

            if (request.amount) {
                if (request.amount <= 0)
                    throw new ValueError("AMOUNT_MUST_BE_GREATER_THAN_0")

                let amount = new Money(request.amount)
                record.setMoney(amount)
            }

            if (request.type) {
                let type = mapperMainTransactionCategory(request.type)
                record.setType(type === TransactionType.INCOME ? RecordType.CREDIT : RecordType.DEBIT)
                transaction.setTransactionType(type)
            }

            if (request.description) {
                record.setDescription(request.description)
            }

            if (request.date) {
                record.setDate(request.date)
            }

            if (request.categoryId) {
                if (!(await this.transationDependencies.categoryRepository?.get(request.categoryId)))
                    throw new ResourceNotFoundError("CATEGORY_NOT_FOUND")

                transaction.setCategoryRef(request.categoryId)
            }
            
            if (request.tagIds) {
                const foundTags = await this.transationDependencies.tagRepository?.getManyByIds(request.tagIds) ?? [];
                if (foundTags.length !== request.tagIds.length)
                    throw new ResourceNotFoundError("TAGS_NOT_FOUND");

                transaction.setTags(request.tagIds)
            }
            
            if (request.budgetIds) {
                const foundBudgets = await this.transationDependencies.budgetRepository?.getManyByIds(request.budgetIds) ?? [];
                if (foundBudgets.length !== request.budgetIds.length)
                    throw new ResourceNotFoundError("BUDGETS_NOT_FOUND");

                transaction.setBudgets(request.budgetIds)
            }

            if (record.hasChange() || transaction.hasChange())  {
                await this.deleteTransactionUsecase.execute(request.id)
                
                await this.addTransactionUsecase.execute({
                    accountId: transaction.getAccountRef(),
                    amount: record.getMoney().getAmount(),
                    categoryId: transaction.getCategoryRef(),
                    tagIds: transaction.getTags(),
                    date: record.getUTCDate(),
                    description: record.getDescription(),
                    type: transaction.getTransactionType(),
                    budgetIds: transaction.getBudgetRefs()
                })
            }

            this.unitOfWork.commit()
        } catch (err) {
            this.unitOfWork.rollback()
            throw err
        }
    }
}
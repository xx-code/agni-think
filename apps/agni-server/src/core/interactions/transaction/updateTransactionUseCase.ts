import { TransactionRepository } from "../../repositories/transactionRepository";
import { FREEZE_CATEGORY_ID, mapperMainTransactionCategory, RecordType, SAVING_CATEGORY_ID, TransactionType } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { RequestAddTransactionUseCase } from "./addTransactionUseCase";
import { ValueError } from "@core/errors/valueError";
import { IUsecase } from "../interfaces";
import { TransactionDependencies } from "../facades";
import { CreatedDto } from "@core/dto/base";
import { MomentDateService } from "@core/domains/entities/libs";


export type RequestUpdateTransactionUseCase = {
    id: string;
    accountId?: string
    tagIds?: string[]
    budgetIds?: string[]
    categoryId?: string
    type?: string
    description?: string
    date?: string
    amount?: number
}


export class UpdateTransactionUseCase implements IUsecase<RequestUpdateTransactionUseCase, void> {
    private transactionRepository: TransactionRepository
    private transationDependencies: TransactionDependencies 
    private unitOfWork: UnitOfWorkRepository

    private addTransactionUsecase: IUsecase<RequestAddTransactionUseCase, CreatedDto>
    private deleteTransactionUsecase: IUsecase<string, void>

    constructor(
        transactionRepository: TransactionRepository,
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

            if ([SAVING_CATEGORY_ID, FREEZE_CATEGORY_ID].includes(transaction.getCategoryRef()))
                throw new ValueError("CANT_UPDATE_TRANSACTION")

            if (request.accountId) {
                if (!await this.transationDependencies.accountRepository?.isExistById(request.accountId))
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
            }

            if (request.description) {
                record.setDescription(request.description)
            }

            if (request.date) {
                let date = MomentDateService.formatDateWithtime(request.date).toISOString()
                record.setDate(date)
            }

            if (request.categoryId) {
                if (!(await this.transationDependencies.categoryRepository?.isCategoryExistById(request.categoryId)))
                    throw new ResourceNotFoundError("CATEGORY_NOT_FOUND")

                transaction.setCategoryRef(request.categoryId)
            }
            
            if (request.tagIds) {
                if (!(await this.transationDependencies.tagRepository?.isTagExistByIds(request.tagIds)))
                    throw new ResourceNotFoundError("TAG_NOT_FOUND")

                transaction.setTags(request.tagIds)
            }
            
            if (request.budgetIds) {
                if (!(await this.transationDependencies.budgetRepository?.isBudgetExistByIds(request.budgetIds)))
                    throw new ResourceNotFoundError("BUDGET_NOT_FOUND")

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
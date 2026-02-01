import { FREEZE_CATEGORY_ID, mapperMainTransactionCategory, RecordType, SAVING_CATEGORY_ID, TransactionType } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { RequestAddTransactionUseCase } from "./addTransactionUseCase";
import { ValueError } from "@core/errors/valueError";
import { IUsecase } from "../interfaces";
import { TransactionDependencies } from "../facades";
import { CreatedDto } from "@core/dto/base";
import Repository, { RecordFilter } from "@core/adapters/repository";
import { Transaction } from "@core/domains/entities/transaction";


export type RequestUpdateTransactionUseCase = {
    id: string;
    accountId?: string
    date?: Date
    type?: string
    currencyId?: string
    removeRecordIds: string[]
    addRecords: {
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
            const trx = await this.unitOfWork.start()
            let transaction = await this.transactionRepository.get(request.id);
            if (!transaction)
                throw new ResourceNotFoundError("TRANSACTION_NOT_FOUND")

            if (request.accountId) {
                if (!await this.transationDependencies.accountRepository?.get(request.accountId))
                    throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")
                transaction.setAccountRef(request.accountId)
            }

            if (request.type) {
                let type = mapperMainTransactionCategory(request.type)
                transaction.setTransactionType(type)
            }

            if (request.date) {
                transaction.setDate(request.date)
            }

            const anyRecordChanged = request.addRecords.length > 0 || request.removeRecordIds.length > 0

            if (anyRecordChanged || transaction.hasChange())  {
                console.log(request.addRecords)
                await this.addTransactionUsecase.execute({
                    accountId: transaction.getAccountRef(),
                    type: transaction.getTransactionType(),
                    date: transaction.getDate(),
                    status: transaction.getStatus(),
                    records: request.addRecords.map(i => ({
                        amount: i.amount,
                        categoryId: i.categoryId,
                        description: i.description,
                        tagIds: i.tagIds,
                        budgetIds: i.budgetIds,
                    })),
                    deductions: []
                }, trx)

                await this.deleteTransactionUsecase.execute(request.id, trx)
            }
            
            await this.unitOfWork.commit()

        } catch (err) {
            console.log(err)
            await this.unitOfWork.rollback()
            throw err
        }
    }
}
import { IUsecase } from "../interfaces";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { TransactionStatus, TransactionType } from "@core/domains/constants";
import Repository, { RecordFilter } from "@core/adapters/repository";
import { Transaction } from "@core/domains/entities/transaction";
import { Account } from "@core/domains/entities/account";
import { Record } from "@core/domains/entities/record";
import { Money } from "@core/domains/entities/money";

export type RequestCompleteTransactionUsecase = {
    transactionId: string
}

export class CompteTransactionUsecase implements IUsecase<RequestCompleteTransactionUsecase, void> {
    private transactionRepo: Repository<Transaction>
    private accountRepo: Repository<Account>
    private recordRepo: Repository<Record>
    private unitOfWork: UnitOfWorkRepository

    constructor(
        transactionRepo: Repository<Transaction>, 
        accountRepo: Repository<Account>, 
        recordRepo: Repository<Record>,
        unitOfWork: UnitOfWorkRepository) 
    {
        this.transactionRepo = transactionRepo
        this.accountRepo = accountRepo
        this.recordRepo = recordRepo
        this.unitOfWork = unitOfWork
    }

    async execute(request: RequestCompleteTransactionUsecase): Promise<void> {
        try {
            const trx = await this.unitOfWork.start()

            const transaction = await this.transactionRepo.get(request.transactionId)
            if (transaction === null)
                throw new ResourceNotFoundError("TRANSACTION_NOT_FOUND")

            const account = await this.accountRepo.get(transaction.getAccountRef())
            if (account === null)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

            const recordFilter = new RecordFilter()
            recordFilter.transactionIds = [request.transactionId]
            const records = await this.recordRepo.getAll({ offset: 0, limit: 0, queryAll: true}, recordFilter)
            const totalAmount = records.items.map(i => i.getMoney().getAmount()).reduce((prev, curr) => curr += prev) 
            // set decution

            if (transaction.getTransactionType() === TransactionType.INCOME)
                account!.addOnBalance(new Money(totalAmount))
            else 
                account!.substractBalance(new Money(totalAmount))

            await this.accountRepo.update(account, trx)

            transaction.setStatus(TransactionStatus.COMPLETE)

            await this.transactionRepo.update(transaction, trx)

            await this.unitOfWork.commit()
        } catch(err) {
            await this.unitOfWork.rollback()
            throw err
        }
    }

}
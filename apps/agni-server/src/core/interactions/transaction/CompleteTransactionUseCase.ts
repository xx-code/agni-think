import { IUsecase } from "../interfaces";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { RecordType, TransactionStatus } from "@core/domains/constants";
import Repository from "@core/adapters/repository";
import { Transaction } from "@core/domains/entities/transaction";
import { Account } from "@core/domains/entities/account";
import { Record } from "@core/domains/entities/record";

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
            // await this.unitOfWork.start()

            const transaction = await this.transactionRepo.get(request.transactionId)
            if (transaction === null)
                throw new ResourceNotFoundError("TRANSACTION_NOT_FOUND")

            const account = await this.accountRepo.get(transaction.getAccountRef())
            if (account === null)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

            const record = await this.recordRepo.get(transaction.getRecordRef())
            if (record === null)
                throw new ResourceNotFoundError("RECORD_NOT_FOUND")

            record.getType() === RecordType.CREDIT ? account!.addOnBalance(record.getMoney()) : account!.substractBalance(record.getMoney())

            await this.accountRepo.update(account)

            transaction.setStatus(TransactionStatus.COMPLETE)

            await this.transactionRepo.update(transaction)

            // await this.unitOfWork.commit()
        } catch(err) {
            // await this.unitOfWork.rollback()
            throw err
        }
    }

}
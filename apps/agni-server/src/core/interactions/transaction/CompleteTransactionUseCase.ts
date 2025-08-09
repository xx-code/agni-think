import { TransactionRepository } from "@core/repositories/transactionRepository";
import { IUsecase } from "../interfaces";
import { AccountRepository } from "@core/repositories/accountRepository";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { RecordType, TransactionStatus } from "@core/domains/constants";
import { RecordRepository } from "@core/repositories/recordRepository";

export type RequestCompleteTransactionUsecase = {
    transactionId: string
}

export class CompteTransactionUsecase implements IUsecase<RequestCompleteTransactionUsecase, void> {
    private transactionRepo: TransactionRepository
    private accountRepo: AccountRepository
    private recordRepo: RecordRepository
    private unitOfWork: UnitOfWorkRepository

    constructor(
        transactionRepo: TransactionRepository, 
        accountRepo: AccountRepository, 
        recordRepo: RecordRepository,
        unitOfWork: UnitOfWorkRepository) 
    {
        this.transactionRepo = transactionRepo
        this.accountRepo = accountRepo
        this.recordRepo = recordRepo
        this.unitOfWork = unitOfWork
    }

    async execute(request: RequestCompleteTransactionUsecase): Promise<void> {
        try {
            await this.unitOfWork.start()

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

            await this.unitOfWork.commit()
        } catch(err) {
            await this.unitOfWork.rollback()
            throw err
        }
    }

}
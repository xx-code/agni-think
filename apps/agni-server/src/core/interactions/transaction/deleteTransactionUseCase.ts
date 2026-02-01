import { SAVING_CATEGORY_ID, TransactionStatus, TransactionType } from "@core/domains/constants";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { ValueError } from "@core/errors/valueError";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { IUsecase } from "../interfaces";
import Repository, { RecordFilter } from "@core/adapters/repository";
import { Transaction } from "@core/domains/entities/transaction";
import { Account } from "@core/domains/entities/account";
import { Record } from "@core/domains/entities/record";
import { Money } from "@core/domains/entities/money";

export class DeleteTransactionUseCase implements IUsecase<string, void> {
    private transRepository: Repository<Transaction>;
    private accountRepo: Repository<Account>;
    private recordRepo: Repository<Record>;
    private unitOfWork: UnitOfWorkRepository

    constructor(
        repo: Repository<Transaction>, 
        recordRepo: Repository<Record>, 
        unitOfWork: UnitOfWorkRepository, 
        accountRepo: Repository<Account>) {
        this.transRepository = repo
        this.accountRepo = accountRepo
        this.recordRepo = recordRepo
        this.unitOfWork = unitOfWork
    }
    
    async execute(id: string, trx?: any): Promise<void> {
        try {   
            let innerTrx = trx
            if (!trx)
                innerTrx = await this.unitOfWork.start() 

            let transaction = await this.transRepository.get(id);
            if (!transaction)
                throw new ResourceNotFoundError('TRANSACTION_NOT_FOUND')

            const extendRecordFilter = new RecordFilter()
            extendRecordFilter.transactionIds = [id]
            const records = await this.recordRepo.getAll({ offset: 0, limit: 0, queryAll: true}, extendRecordFilter)

            if (records.items.map(i => i.getCategoryRef()).includes(SAVING_CATEGORY_ID))
                throw new ValueError("DELETE_SAVING_TRANSACTION")

            let account = await this.accountRepo.get(transaction.getAccountRef())
            if (account === null)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

            if (transaction.getStatus() === TransactionStatus.COMPLETE) {
                const recordFilter = new RecordFilter()
                recordFilter.transactionIds = [transaction.getId()]
                const records = await this.recordRepo.getAll({ offset: 0, limit: 0, queryAll: true}, recordFilter)
                const totalAmount = records.items.map(i => i.getMoney().getAmount()).reduce((prev, curr) => curr += prev) 

                if (transaction.getTransactionType() === TransactionType.INCOME) {
                    account.addOnBalance(new Money(totalAmount))
                } else {
                    account.substractBalance(new Money(totalAmount)) 
                }

                await this.accountRepo.update(account, innerTrx);   
            }

            for (let i = 0; i < records.items.length; i++)
                await this.recordRepo.delete(records.items[i].getId(), innerTrx)

            await this.transRepository.delete(id, innerTrx);
            
            if (!trx)
                await this.unitOfWork.commit()

        } catch(err) {
            if (!trx)
                await this.unitOfWork.rollback()
            
            throw err
        }
    }
}
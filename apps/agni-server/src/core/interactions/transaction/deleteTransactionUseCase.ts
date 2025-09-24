import { RecordType, SAVING_CATEGORY_ID, TransactionStatus } from "@core/domains/constants";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { ValueError } from "@core/errors/valueError";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { IUsecase } from "../interfaces";
import Repository from "@core/adapters/repository";
import { Transaction } from "@core/domains/entities/transaction";
import { Account } from "@core/domains/entities/account";
import { Record } from "@core/domains/entities/record";

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
    
    async execute(id: string): Promise<void> {
        try {   
            await this.unitOfWork.start()

            let transaction = await this.transRepository.get(id);
            if (!transaction)
                throw new ResourceNotFoundError('TRANSACTION_NOT_FOUND')

            if ([SAVING_CATEGORY_ID].includes(transaction.getCategoryRef()))
                throw new ValueError("CANT_UPDATE_TRANSACTION")

            let record = await this.recordRepo.get(transaction.getRecordRef())
            if (record === null)
                throw new ResourceNotFoundError("RECORD_NOT_FOUND")

            let account = await this.accountRepo.get(transaction.getAccountRef())
            if (account === null)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")

            if (transaction.getStatus() === TransactionStatus.COMPLETE) {
                record.getType() === RecordType.CREDIT ? account.substractBalance(record.getMoney()) : account.addOnBalance(record.getMoney())
                await this.accountRepo.update(account);
            }
            
            await this.recordRepo.delete(record.getId())

            await this.transRepository.delete(id);
            
            await this.unitOfWork.commit()
        } catch(err) {
            await this.unitOfWork.rollback()
            throw err
        }
    }
}
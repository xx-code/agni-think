import { RecordType, SAVING_CATEGORY_ID } from "@core/domains/constants";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { ValueError } from "@core/errors/valueError";
import { AccountRepository } from "@core/repositories/accountRepository";
import { RecordRepository } from "@core/repositories/recordRepository";
import { TransactionRepository } from "@core/repositories/transactionRepository";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { IUsecase } from "../interfaces";

export class DeleteTransactionUseCase implements IUsecase<string, void> {
    private transRepository: TransactionRepository;
    private accountRepo: AccountRepository;
    private recordRepo: RecordRepository;
    private unitOfWork: UnitOfWorkRepository

    constructor(
        repo: TransactionRepository, 
        recordRepo: RecordRepository, 
        unitOfWork: UnitOfWorkRepository, 
        accountRepo: AccountRepository) {
        this.transRepository = repo
        this.accountRepo = accountRepo
        this.recordRepo = recordRepo
        this.unitOfWork = unitOfWork
    }
    
    async execute(id: string): Promise<void> {
        try {   
            await this.unitOfWork.start()

            if (!(await this.transRepository.isTransactionExistById(id)))
                throw new ResourceNotFoundError('TRANSACTION_NOT_FOUND')

            let transaction = await this.transRepository.get(id);

            if ([SAVING_CATEGORY_ID].includes(transaction.getCategoryRef()))
                throw new ValueError("CANT_UPDATE_TRANSACTION")

            let record = await this.recordRepo.get(transaction.getRecordRef())
            if (record === null)
                throw new ResourceNotFoundError("RECORD_NOT_FOUND")

            let account = await this.accountRepo.get(transaction.getAccountRef())
            if (account === null)
                throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND")
            
            record.getType() === RecordType.CREDIT ? account.substractBalance(record.getMoney()) : account.addOnBalance(record.getMoney())

            await this.accountRepo.update(account)
            
            await this.recordRepo.delete(record.getId())

            await this.transRepository.delete(id);
            
            await this.unitOfWork.commit()
        } catch(err) {
            await this.unitOfWork.rollback()
            throw err
        }
    }
}
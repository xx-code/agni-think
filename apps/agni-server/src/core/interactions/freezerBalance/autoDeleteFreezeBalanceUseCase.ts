import { AccountRepository } from "@core/repositories/accountRepository";
import { RecordRepository } from "@core/repositories/recordRepository";
import { TransactionRepository, TransactionFilter } from "@core/repositories/transactionRepository";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { MomentDateService } from "@core/domains/entities/libs";

export class AutoDeleteFreezeBalanceUseCase  implements IUsecase<void, void> {
    private accountRepository: AccountRepository
    private transactionRepository: TransactionRepository;
    private recordRepository: RecordRepository
    private unitOfWork: UnitOfWorkRepository

    constructor(
        accountRepository: AccountRepository, 
        transactionRepository: TransactionRepository, 
        recordRepository: RecordRepository, 
        unitOfWork: UnitOfWorkRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.recordRepository = recordRepository;
        this.unitOfWork = unitOfWork
    }

    async execute(): Promise<void> {
        try {
            let filters: TransactionFilter = {
                offset: 0,
                limit: 0,
                isFreeze: true,
                queryAll: true
            };
      
            let response = await this.transactionRepository.getTransactions(filters);

            this.unitOfWork.start()

            for (let i = 0; i < response.length ; i++) {
                let account = await this.accountRepository.get(response[i].getAccountRef())
                if (account === null) {
                    console.log((new ResourceNotFoundError("ACCOUNT_NOT_FOUND")))
                    continue
                }

                let record = await this.recordRepository.get(response[i].getRecordRef())
                if (record === null) {
                    console.log((new ResourceNotFoundError("RECORD_NOT_FOUND")))
                    continue
                }

                account.addOnBalance(record.getMoney())

                if (MomentDateService.compareDate(MomentDateService.getToday(), record.getUTCDate()) >= 0) {
                    await this.accountRepository.update(account)
                    await this.recordRepository.delete(record.getId())
                    await this.transactionRepository.delete(response[i].getId())
                }
            }
            
            this.unitOfWork.commit()
        } catch (err) {
            this.unitOfWork.rollback()
            console.log(err)
        }
    }
}
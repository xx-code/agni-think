import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { MomentDateService } from "@core/domains/entities/libs";
import Repository, { TransactionFilter } from "@core/adapters/repository";
import { Account } from "@core/domains/entities/account";
import { Transaction } from "@core/domains/entities/transaction";
import { Record } from "@core/domains/entities/record";
import { IEventRegister } from "@core/adapters/event";

export class AutoDeleteFreezeBalanceUseCase  implements IUsecase<void, void> {
    private accountRepository: Repository<Account>
    private transactionRepository: Repository<Transaction>;
    private recordRepository: Repository<Record>
    private unitOfWork: UnitOfWorkRepository
    private eventManager: IEventRegister

    constructor(
        accountRepository: Repository<Account>, 
        transactionRepository: Repository<Transaction>, 
        recordRepository: Repository<Record>, 
        unitOfWork: UnitOfWorkRepository,
        eventManager: IEventRegister) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.recordRepository = recordRepository;
        this.unitOfWork = unitOfWork
        this.eventManager = eventManager
    }

    async execute(): Promise<void> {
        try {
            await this.unitOfWork.start()

            const extendFilter = new TransactionFilter()
            extendFilter.isFreeze = true
            let response = await this.transactionRepository.getAll({
                offset: 0,
                limit: 0,
                queryAll: true
            }, extendFilter);

            for (let i = 0; i < response.items.length ; i++) {
                let account = await this.accountRepository.get(response.items[i].getAccountRef())
                if (account === null) {
                    console.log((new ResourceNotFoundError("ACCOUNT_NOT_FOUND")))
                    continue
                }

                let record = await this.recordRepository.get(response.items[i].getRecordRef())
                if (record === null) {
                    console.log((new ResourceNotFoundError("RECORD_NOT_FOUND")))
                    continue
                }

                account.addOnBalance(record.getMoney())

                if (MomentDateService.compareDate(MomentDateService.getToday(), record.getUTCDate()) >= 0) {
                    await this.accountRepository.update(account)
                    await this.recordRepository.delete(record.getId())
                    await this.transactionRepository.delete(response.items[i].getId())
                    this.eventManager.notify('notification', {
                        title: 'Transaction geler',
                        content:`Le compte ${account.getTitle()} a une transaction degele`
                    })
                }
            }
            
            await this.unitOfWork.commit()
        } catch (err) {
            await this.unitOfWork.rollback()
        }
    }
}
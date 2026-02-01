import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { MomentDateService } from "@core/domains/entities/libs";
import Repository, { RecordFilter, TransactionFilter } from "@core/adapters/repository";
import { Account } from "@core/domains/entities/account";
import { Transaction } from "@core/domains/entities/transaction";
import { Record } from "@core/domains/entities/record";
import { IEventRegister } from "@core/adapters/event";
import { Money } from "@core/domains/entities/money";

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
            // const trx = await this.unitOfWork.start()

            const extendFilter = new TransactionFilter()
            extendFilter.isFreeze = true
            let response = await this.transactionRepository.getAll({
                offset: 0,
                limit: 0,
                queryAll: true
            }, extendFilter);

            for (let i = 0; i < response.items.length ; i++) {
                const transaction = response.items[i]
                let account = await this.accountRepository.get(transaction.getAccountRef())
                if (account === null) {
                    console.log((new ResourceNotFoundError("ACCOUNT_NOT_FOUND")))
                    continue
                }

                const extendRecordFilter = new RecordFilter()
                extendRecordFilter.transactionIds = [transaction.getId()]
                const records = await this.recordRepository.getAll({offset: 0, limit: 0, queryAll: true}, extendRecordFilter)
                const subTotal = records.items.map(i => i.getMoney().getAmount()).reduce((prev, curr) => curr += prev) ?? 0
                // total 

                account.addOnBalance(new Money(subTotal))

                if (MomentDateService.compareDate(MomentDateService.getToday(), transaction.getDate()) >= 0) {
                    await this.accountRepository.update(account, /*trx*/)

                    for (let i = 0; i < records.items.length; i++)
                        await this.recordRepository.delete(records.items[i].getId(), /*trx*/)

                    await this.transactionRepository.delete(response.items[i].getId(), /*trx*/)
                    this.eventManager.notify('notification', {
                        title: 'Transaction geler',
                        content:`Le compte ${account.getTitle()} a une transaction degele`
                    })
                }
            }
            
            // await this.unitOfWork.commit()
        } catch (err) {
            // await this.unitOfWork.rollback()
            console.log(err)
        }
    }
}
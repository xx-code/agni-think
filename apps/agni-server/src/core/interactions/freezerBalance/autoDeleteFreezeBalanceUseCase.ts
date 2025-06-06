import { DateService } from "@core/adapters/libs";
import { FREEZE_CATEGORY_ID } from "@core/domains/constants";
import { AccountRepository } from "@core/repositories/accountRepository";
import { RecordRepository } from "@core/repositories/recordRepository";
import { TransactionRepository, TransactionFilter, SortBy } from "@core/repositories/transactionRepository";
import { UnitOfWorkRepository } from "@core/repositories/unitOfWorkRepository";

export interface IAutoDeleteFreezeBalanceUseCase {
    execute(): void
}

export interface IAutoDeleteFreezeBalancePresenter {
    success(message: string): void;
    fail(err: Error): void;
}

export class AutoDeleteFreezeBalanceUseCase  implements IAutoDeleteFreezeBalanceUseCase {
    private accountRepository: AccountRepository
    private transactionRepository: TransactionRepository;
    private recordRepository: RecordRepository
    private unitOfWork: UnitOfWorkRepository
    private dateService: DateService
    private presenter: IAutoDeleteFreezeBalancePresenter;

    constructor(accountRepository: AccountRepository, transactionRepository: TransactionRepository, recordRepository: RecordRepository, unitOfWork: UnitOfWorkRepository, dateService: DateService, presenter: IAutoDeleteFreezeBalancePresenter) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.recordRepository = recordRepository;
        this.unitOfWork = unitOfWork
        this.dateService = dateService
        this.presenter = presenter;
    }

    async execute(): Promise<void> {
        try {
            let categoriesIds = [FREEZE_CATEGORY_ID]

        
            let filters: TransactionFilter = {
                accounts: [], 
                tags: [],
                budgets: [],
                categories: categoriesIds,
                startDate: '',
                endDate: '',
                types: [],
                minPrice: null,
                maxPrice: null
            };

            let sortBy: SortBy|null = null;
      
            let response = await this.transactionRepository.getPaginations(-1, 1, sortBy, filters);

            this.unitOfWork.start()
            for (let i = 0; i < response.transactions.length ; i++) {
                let account = await this.accountRepository.get(response.transactions[i].getAccountRef())
                let record = await this.recordRepository.get(response.transactions[i].getRecordRef())

                account.addOnBalance(record.getMoney())

                if (this.dateService.compareDate(this.dateService.getToday(), record.getDate()) >= 0) {
                    await this.accountRepository.update(account)
                    await this.recordRepository.delete(record.getId())
                    await this.transactionRepository.delete(response.transactions[i].getId())
                }
            }
            
            this.unitOfWork.commit()
            this.presenter.success("done");
        } catch (err) {
            this.unitOfWork.rollback()
            this.presenter.fail(err as Error);
        }
    }
}
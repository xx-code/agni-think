import { mapperTransactionType } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import { TransactionType } from "@core/domains/entities/record";
import { isEmpty, DateParser } from "@core/domains/helpers";
import ValidationError from "@core/errors/validationError";
import { RecordRepository } from "@core/repositories/recordRepository";
import { TransactionRepository, TransactionFilter } from "@core/repositories/transactionRepository";

export type RequestGetBalanceBy = {
    accountsIds: string[],
    tagsIds: string[],
    categoriesIds: string[],
    dateStart: string,
    dateEnd: string,
    type: string,
    minPrice: number,
    maxPrice: number
}

export interface IGetBalanceByUseCase {
    execute(request: RequestGetBalanceBy): void;
}

export interface IGetBalanceByUseCaseResponse {
    success(balance: number): void;
    fail(err: Error): void
}

export class GetBalanceByUseCase implements IGetBalanceByUseCase {
    private transactionRepository: TransactionRepository;
    private recordRepository: RecordRepository
    private presenter: IGetBalanceByUseCaseResponse;

    constructor(transaction_repo: TransactionRepository, recordRepository: RecordRepository, presenter: IGetBalanceByUseCaseResponse) {
        this.transactionRepository = transaction_repo;
        this.recordRepository = recordRepository
        this.presenter = presenter;
    }

    async execute(request: RequestGetBalanceBy): Promise<void> {
        try {
            if (!isEmpty(request.dateStart) && !isEmpty(request.dateEnd)) {
                // compare date
                if (DateParser.fromString(request.dateEnd!).compare(DateParser.fromString(request.dateStart!)) < 0) {
                    throw new ValidationError('Date start must be less than date end');
                }
            }

            let type = null;
            if (!isEmpty(request.type)) {
                type = mapperTransactionType(request.type!)
            }

            let minPrice = null;
            if (!isEmpty(request.minPrice))
                minPrice  = new Money(request.minPrice)

            let maxPrice = null;
            if (!isEmpty(request.maxPrice))
                maxPrice = new Money(request.maxPrice)

            let filter: TransactionFilter = {
                accounts: request.accountsIds,
                categories: request.categoriesIds,
                tags: request.tagsIds,
                startDate: request.dateStart,
                endDate: request.dateEnd,
                type: type,
                minPrice: minPrice,
                maxPrice: maxPrice
            }

            let transactions = await this.transactionRepository.getTransactions(filter);
            

            let records = await this.recordRepository.getManyById(transactions.map(transaction => transaction.getRecordRef()))
            let balance = 0
            for (let record of records) {
                if (record.getType() === TransactionType.CREDIT)
                    balance += record.getMoney().getAmount()
                else 
                    balance -= record.getMoney().getAmount()
            }

            this.presenter.success(balance);
        } catch(err) {
            this.presenter.fail(err as Error);
        }
    }
}
import { DateService } from "@core/adapters/libs";
import { mapperMainTransactionCategory, mapperTransactionType } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import { TransactionType } from "@core/domains/entities/record";
import { isEmpty, DateParser } from "@core/domains/helpers";
import ValidationError from "@core/errors/validationError";
import { RecordRepository } from "@core/repositories/recordRepository";
import { TransactionRepository, TransactionFilter } from "@core/repositories/transactionRepository";

export type RequestGetBalanceBy = {
    accountsIds: string[],
    tagsIds: string[],
    budgetIds: string[],
    categoriesIds: string[],
    dateStart: string,
    dateEnd: string,
    types: string[],
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
    private dateService: DateService
    private presenter: IGetBalanceByUseCaseResponse;

    constructor(dateService: DateService, transaction_repo: TransactionRepository, recordRepository: RecordRepository, presenter: IGetBalanceByUseCaseResponse) {
        this.transactionRepository = transaction_repo;
        this.recordRepository = recordRepository
        this.dateService = dateService
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

            let types = []
            if (!isEmpty(request.types))
            {
                for(const type of request.types) {
                    types.push(mapperMainTransactionCategory(type))
                }
            }

            let minPrice = null;
            if (!isEmpty(request.minPrice))
                minPrice  = new Money(request.minPrice)

            let maxPrice = null;
            if (!isEmpty(request.maxPrice))
                maxPrice = new Money(request.maxPrice)

            let dateStart = ''
            if (request.dateStart)
                dateStart = this.dateService.formatDate(request.dateStart)

            let dateEnd = ''
            if (request.dateEnd)
                dateEnd = this.dateService.formatDate(request.dateStart)

            let filter: TransactionFilter = {
                accounts: request.accountsIds,
                categories: request.categoriesIds,
                budgets: request.budgetIds,
                tags: request.tagsIds,
                startDate: dateStart,
                endDate: dateEnd,
                types: types,
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

            this.presenter.success(Number(balance.toFixed(2)));
        } catch(err) {
            this.presenter.fail(err as Error);
        }
    }
}
import { DateService } from "@core/adapters/libs";
import { mapperMainTransactionCategory, mapperTransactionType, RecordType } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import { isEmpty, DateParser } from "@core/domains/helpers";
import ValidationError from "@core/errors/validationError";
import { RecordRepository } from "@core/repositories/recordRepository";
import { TransactionRepository, TransactionFilter } from "@core/repositories/transactionRepository";
import { IUsecase } from "../interfaces";
import { MomentDateService } from "@core/domains/entities/libs";

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

export class GetBalanceByUseCase implements IUsecase<RequestGetBalanceBy, number> {
    private transactionRepository: TransactionRepository;
    private recordRepository: RecordRepository
    private dateService: DateService

    constructor(
        dateService: DateService, 
        transaction_repo: TransactionRepository, 
        recordRepository: RecordRepository) {
        this.transactionRepository = transaction_repo;
        this.recordRepository = recordRepository
        this.dateService = dateService
    }

    async execute(request: RequestGetBalanceBy): Promise<number> {
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
            dateStart = MomentDateService.formatDate(request.dateStart).toString()

        let dateEnd = ''
        if (request.dateEnd)
            dateEnd = this.dateService.formatDate(request.dateStart).toString()

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
            if (record.getType() === RecordType.CREDIT)
                balance += record.getMoney().getAmount()
            else 
                balance -= record.getMoney().getAmount()
        }

        return Number(balance.toFixed(2))
    }
}
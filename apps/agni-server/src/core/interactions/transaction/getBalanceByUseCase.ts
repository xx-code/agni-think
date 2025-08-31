import { mapperMainTransactionCategory, RecordType, TransactionStatus } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import ValidationError from "@core/errors/validationError";
import { RecordRepository } from "@core/repositories/recordRepository";
import { TransactionRepository, TransactionFilter } from "@core/repositories/transactionRepository";
import { IUsecase } from "../interfaces";
import { MomentDateService } from "@core/domains/entities/libs";

export type RequestGetBalanceBy = {
    accountIds?: string[],
    tagsIds?: string[],
    budgetIds?: string[],
    categoriesIds?: string[],
    dateStart?: Date,
    dateEnd?: Date,
    types?: string[],
    minPrice?: number,
    maxPrice?: number
}

export class GetBalanceByUseCase implements IUsecase<RequestGetBalanceBy, number> {
    private transactionRepository: TransactionRepository;
    private recordRepository: RecordRepository

    constructor(
        transaction_repo: TransactionRepository, 
        recordRepository: RecordRepository) {
        this.transactionRepository = transaction_repo;
        this.recordRepository = recordRepository
    }

    async execute(request: RequestGetBalanceBy): Promise<number> {
        if (request.dateStart && request.dateEnd) {
            // compare date
            if (MomentDateService.compareDate(request.dateEnd, request.dateStart) < 0) {
                throw new ValidationError('Date start must be less than date end');
            }
        }

        let types = []
        if (request.types)
        {
            for(const type of request.types) {
                types.push(mapperMainTransactionCategory(type))
            }
        }

        let minPrice;
        if (request.minPrice)
            minPrice  = new Money(request.minPrice)

        let maxPrice;
        if (request.maxPrice)
            maxPrice = new Money(request.maxPrice)

        let dateStart;
        if (request.dateStart)
            dateStart = request.dateStart

        let dateEnd;
        if (request.dateEnd)
            dateEnd = request.dateEnd


        let filter: TransactionFilter = {
            offset: 0,
            limit: 0,
            queryAll: true,
            accounts: request.accountIds || [],
            categories: request.categoriesIds || [],
            budgets: request.budgetIds || [],
            tags: request.tagsIds || [],
            startDate: dateStart,
            endDate: dateEnd,
            types: types,
            minPrice: minPrice,
            maxPrice: maxPrice
        }

        let transactions = await this.transactionRepository.getTransactions(filter);

        let records = await this.recordRepository
            .getManyById(transactions.filter(i => i.getStatus() == TransactionStatus.COMPLETE)
            .map(transaction => transaction.getRecordRef()))
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
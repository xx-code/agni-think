import { mapperMainTransactionCategory, mapperTransactionType, RecordType, TransactionStatus } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import ValidationError from "@core/errors/validationError";
import { IUsecase } from "../interfaces";
import { MomentDateService } from "@core/domains/entities/libs";
import Repository, { TransactionFilter } from "@core/adapters/repository";
import { Transaction } from "@core/domains/entities/transaction";
import { Record } from "@core/domains/entities/record";
import { RequestGetPagination } from "./getPaginationTransactionUseCase";


export class GetBalanceByUseCase implements IUsecase<RequestGetPagination, number> {
    private transactionRepository: Repository<Transaction>;
    private recordRepository: Repository<Record>

    constructor(
        transaction_repo: Repository<Transaction>, 
        recordRepository: Repository<Record>) {
        this.transactionRepository = transaction_repo;
        this.recordRepository = recordRepository
    }

    async execute(request: RequestGetPagination): Promise<number> {
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


        let filter = {
            offset: 0,
            limit: 0,
            queryAll: true,
        }


        const extendTransactionFilter = new TransactionFilter()
        extendTransactionFilter.accounts = request.accountFilterIds
        extendTransactionFilter.categories = request.categoryFilterIds
        extendTransactionFilter.budgets = request.budgetFilterIds
        extendTransactionFilter.tags = request.tagFilterIds
        extendTransactionFilter.startDate = request.dateStart
        extendTransactionFilter.endDate = request.dateEnd
        extendTransactionFilter.isFreeze = undefined
        extendTransactionFilter.types = types

        const transactions = await this.transactionRepository.getAll(filter, extendTransactionFilter);
        console.log(transactions.total)

        let records = await this.recordRepository
            .getManyByIds(transactions.items.filter(i => i.getStatus() == TransactionStatus.COMPLETE)
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
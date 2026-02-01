import { mapperMainTransactionCategory, RecordType, TransactionStatus, TransactionType } from "@core/domains/constants";
import { Money } from "@core/domains/entities/money";
import ValidationError from "@core/errors/validationError";
import { IUsecase } from "../interfaces";
import { MomentDateService } from "@core/domains/entities/libs";
import Repository, { RecordFilter, TransactionFilter } from "@core/adapters/repository";
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
        extendTransactionFilter.startDate = request.dateStart
        extendTransactionFilter.endDate = request.dateEnd
        extendTransactionFilter.isFreeze = false
        extendTransactionFilter.types = types

        const transactions = await this.transactionRepository.getAll(filter, extendTransactionFilter);

        let balance = 0
        for(const transaction of transactions.items) {
            const extendRecordFilter = new RecordFilter()
            extendRecordFilter.transactionIds = [transaction.getId()]
            extendRecordFilter.categories = request.categoryFilterIds
            extendRecordFilter.budgets = request.budgetFilterIds
            extendRecordFilter.tags = request.tagFilterIds
            const records = await this.recordRepository.getAll(filter, extendRecordFilter)
            let subTotal = 0
            if (records.items.length > 0)
                subTotal = records.items.map(i => i.getMoney().getAmount()).reduce((prev, curr) => curr += prev) ?? 0

            balance = transaction.getTransactionType() === TransactionType.INCOME ? balance + subTotal : balance - subTotal
        }


        return Number(balance.toFixed(2))
    }
}
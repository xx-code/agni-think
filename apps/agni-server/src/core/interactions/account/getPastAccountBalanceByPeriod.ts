import { IUsecase } from "../interfaces";
import { mapperPeriod, Period, RecordType, TransactionStatus, TransactionType } from "@core/domains/constants";
import { MomentDateService } from "@core/domains/entities/libs";
import Repository, { RecordFilter, TransactionFilter } from "@core/adapters/repository";
import { Transaction } from "@core/domains/entities/transaction";
import { Record } from "@core/domains/entities/record";
import { QueryFilterAllRepository } from "@core/repositories/dto";


export type GetAccountBalanceByPeriodDto = {
    accountId: string
    balance: number
}

export type RequestGetAccountBalanceByPeriod = {
    period: string 
    periodTime: number
    accountIds: string[]
}


export class GetPastAccountBalanceByPeriodUseCase implements IUsecase<RequestGetAccountBalanceByPeriod, GetAccountBalanceByPeriodDto[]>{
    private transactionRepository: Repository<Transaction>;
    private recordRepository: Repository<Record>

    constructor(
        transactionRepo: Repository<Transaction>,
        recordRepository: Repository<Record>
    ) {
        this.transactionRepository = transactionRepo;
        this.recordRepository = recordRepository;
    }

    async execute(request: RequestGetAccountBalanceByPeriod): Promise<GetAccountBalanceByPeriodDto[]> {
        const period = mapperPeriod(request.period)
        const beginDate = MomentDateService.getUTCDateSubstraction(new Date(), period, request.periodTime) 
        const { startDate, endDate } = MomentDateService.getUTCDateByPeriod(beginDate, period, request.periodTime)
        
        const response: GetAccountBalanceByPeriodDto[]= []
        for (let accountId of request.accountIds) {

            const filter: QueryFilterAllRepository = {
                offset: 0,
                limit: 0,
                queryAll: true,
            }
            const filterExtends = new TransactionFilter()
            filterExtends.accounts = [accountId]
            filterExtends.startDate = startDate
            filterExtends.endDate = endDate
            filterExtends.status = TransactionStatus.COMPLETE
            let transactions = await this.transactionRepository.getAll(filter, filterExtends);

            let balance = 0

            for(const transaction of transactions.items) {
                const recordExtendFilter = new RecordFilter()
                recordExtendFilter.transactionIds = [transaction.getId()]
                const records = await this.recordRepository.getAll({offset: 0, limit: 0, queryAll: true}, recordExtendFilter)
                const subTotal = records.items.map(i => i.getMoney().getAmount()).reduce((prev, curr) => curr += prev) ?? 0

                balance = transaction.getTransactionType() === TransactionType.INCOME ? balance + subTotal : balance - subTotal
            }

            response.push({
                accountId: accountId, 
                balance: Number(balance.toFixed(2)), 
            });
        }

        return response 
    }
}
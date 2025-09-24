import { IUsecase } from "../interfaces";
import { mapperPeriod, Period, RecordType, TransactionStatus } from "@core/domains/constants";
import { MomentDateService } from "@core/domains/entities/libs";
import Repository, { TransactionFilter } from "@core/adapters/repository";
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

            let transactions = await this.transactionRepository.getAll(filter, filterExtends);

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

            response.push({
                accountId: accountId, 
                balance: Number(balance.toFixed(2)), 
            });
        }

        return response 
    }
}
import { IUsecase } from "../interfaces";
import { mapperPeriod, Period, RecordType, TransactionStatus } from "@core/domains/constants";
import { TransactionFilter, TransactionRepository } from "@core/repositories/transactionRepository";
import { RecordRepository } from "@core/repositories/recordRepository";
import { MomentDateService } from "@core/domains/entities/libs";


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
    private transactionRepository: TransactionRepository;
    private recordRepository: RecordRepository

    constructor(
        transactionRepo: TransactionRepository,
        recordRepository: RecordRepository
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

            const filter: TransactionFilter = {
                accounts: [accountId],
                startDate: startDate,
                endDate: endDate
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

            response.push({
                accountId: accountId, 
                balance: Number(balance.toFixed(2)), 
            });
        }

        return response 
    }
}
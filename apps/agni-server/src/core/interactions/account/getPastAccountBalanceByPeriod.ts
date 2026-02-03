import { IUsecase } from "../interfaces";
import { mapperPeriod, Period, RecordType, TransactionStatus, TransactionType } from "@core/domains/constants";
import { MomentDateService } from "@core/domains/entities/libs";
import Repository, { RecordFilter, TransactionFilter } from "@core/adapters/repository";
import { Transaction } from "@core/domains/entities/transaction";
import { Record } from "@core/domains/entities/record";
import { QueryFilterAllRepository } from "@core/repositories/dto";
import { RequestGetPagination } from "../transaction/getPaginationTransactionUseCase";
import { GetBalanceDto } from "../transaction/getBalanceByUseCase";


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
    private getBalanceUc: IUsecase<RequestGetPagination, GetBalanceDto>

    constructor(
        transactionRepo: Repository<Transaction>,
        recordRepository: Repository<Record>,
        getBalanceUc: IUsecase<RequestGetPagination, GetBalanceDto>
    ) {
        this.transactionRepository = transactionRepo;
        this.recordRepository = recordRepository;
        this.getBalanceUc = getBalanceUc
    }

    async execute(request: RequestGetAccountBalanceByPeriod): Promise<GetAccountBalanceByPeriodDto[]> {
        const period = mapperPeriod(request.period)
        const beginDate = MomentDateService.getUTCDateSubstraction(new Date(), period, request.periodTime) 
        const { startDate, endDate } = MomentDateService.getUTCDateByPeriod(beginDate, period, request.periodTime)
        
        const response: GetAccountBalanceByPeriodDto[]= []
        for (let accountId of request.accountIds) {

            const res = await this.getBalanceUc.execute({
                offset: 0,
                limit: 0,
                accountFilterIds: [accountId],
                dateStart: startDate,
                dateEnd: endDate,
                status: TransactionStatus.COMPLETE
            }) 

            response.push({
                accountId: accountId, 
                balance: res.balance, 
            });
        }

        return response 
    }
}
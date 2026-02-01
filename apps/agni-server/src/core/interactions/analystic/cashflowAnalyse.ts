import { IUsecase } from "../interfaces"
import { 
    FREEZE_CATEGORY_ID, 
    mapperPeriod, 
    Period, 
    RecordType, 
    SAVING_CATEGORY_ID, 
    TRANSFERT_CATEGORY_ID } from "@core/domains/constants"
import UnExpectedError from "@core/errors/unExpectedError"
import { MomentDateService } from "@core/domains/entities/libs"
import Repository, { RecordFilter, TransactionFilter } from "@core/adapters/repository"
import { Transaction } from "@core/domains/entities/transaction"
import { Record } from "@core/domains/entities/record"

export type RequestCashFlow = {
    period: string
    periodTime: number
    showNumber: number
}

export type CashFlowResponse = {
    spendFlow: number[]
    gainsFlow: number[]
}

export class CashFlowAnalyseUseCase implements IUsecase<RequestCashFlow, CashFlowResponse> {
    private transRepo: Repository<Transaction>
    private recordRepo: Repository<Record>

    constructor(
        transactionRepository: Repository<Transaction>,
        recordRepo: Repository<Record>
    ) {
        this.transRepo = transactionRepository
        this.recordRepo = recordRepo
    }
    
    async execute(request: RequestCashFlow): Promise<CashFlowResponse> {
        const period = mapperPeriod(request.period)
        if (period === Period.DAY || (period === Period.WEEK && request.periodTime === 1))
            throw new UnExpectedError("BAD_PERIOD_SELECTION")

        if (request.showNumber <= 0)
            throw new UnExpectedError("SHOWNUMBER_MUST_BE_GREATER_THAN_)")

        const gainsFlows: number[] = []
        const spendFlows: number[] = []
        
        const today = new Date(Date.now()) 

        const categoriesToExludes = [FREEZE_CATEGORY_ID, TRANSFERT_CATEGORY_ID, SAVING_CATEGORY_ID, ]

        for(let i = request.showNumber; i > 0; i--)
        {
            const beginDate = MomentDateService.getUTCDateSubstraction(today, period, 
                (request.periodTime * (i - 1))) 

            const { startDate, endDate } = MomentDateService.getUTCDateByPeriod(beginDate, period, request.periodTime)
            const extendFilter = new TransactionFilter()
            extendFilter.startDate = startDate
            extendFilter.endDate = endDate
            const transactions = await this.transRepo.getAll({
                offset: 0,
                limit: 0,
                queryAll: true
            }, extendFilter);

            const recordExtendFilter = new RecordFilter()
            recordExtendFilter.transactionIds = transactions.items.map(i => i.getId()) 
            recordExtendFilter.transactionIds = transactions.items.map(transaction => transaction.getId())
            let records = await this.recordRepo.getAll({offset: 0, limit: 0, queryAll: true}, recordExtendFilter)
            const recordsFilter = records.items.filter(i => !categoriesToExludes.includes(i.getCategoryRef()))

            const spends = recordsFilter.filter(i => i.getType() === RecordType.DEBIT)
            .reduce((acc: number, record) => {
                return acc + record.getMoney().getAmount();
            }, 0)

            const gains = recordsFilter.filter(i => i.getType() === RecordType.CREDIT).reduce((acc: number, record) => {
                return acc + record.getMoney().getAmount();
            }, 0)

            gainsFlows.push(gains)
            spendFlows.push(spends)
        } 

        return {
            gainsFlow: gainsFlows,
            spendFlow: spendFlows
        }
    }
}
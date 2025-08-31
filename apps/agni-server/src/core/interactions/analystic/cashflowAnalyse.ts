import { TransactionRepository } from "@core/repositories/transactionRepository"
import { IUsecase } from "../interfaces"
import { FREEZE_CATEGORY_ID, mapperPeriod, Period, RecordType, SAVING_CATEGORY_ID, TRANSFERT_CATEGORY_ID } from "@core/domains/constants"
import UnExpectedError from "@core/errors/unExpectedError"
import { MomentDateService } from "@core/domains/entities/libs"
import { RecordRepository } from "@core/repositories/recordRepository"

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
    private transRepo: TransactionRepository
    private recordRepo: RecordRepository

    constructor(
        transactionRepository: TransactionRepository,
        recordRepo: RecordRepository
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
            let transactions = await this.transRepo.getTransactions({
                startDate: startDate,
                endDate: endDate,
                offset: 0,
                limit: 0,
                queryAll: true
            });

            transactions = transactions.filter(i => !categoriesToExludes.includes(i.getCategoryRef()))


            const records = await this.recordRepo.getManyById(transactions.map(i => i.getRecordRef()))

            const spends = records.filter(i => i.getType() === RecordType.DEBIT)
            .reduce((acc: number, record) => {
                return acc + record.getMoney().getAmount();
            }, 0)

            const gains = records.filter(i => i.getType() === RecordType.CREDIT).reduce((acc: number, record) => {
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
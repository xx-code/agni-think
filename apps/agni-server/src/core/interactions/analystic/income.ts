import Repository, { RecordFilter, TransactionFilter } from "@core/adapters/repository";
import { IUsecase } from "../interfaces";
import { Record } from "@core/domains/entities/record";
import { Transaction } from "@core/domains/entities/transaction";
import { mapperPeriod, Period, TransactionType } from "@core/domains/constants";
import UnExpectedError from "@core/errors/unExpectedError";
import { MomentDateService } from "@core/domains/entities/libs";

export type RequestIncomAnalystic = {
    period: string
    periodTime: number
    showNumber: number
}

export type IncomeByDescription = {
    label: string
    income: number
}

export type IncomeAnalysticResponse = {
    incomes: number[]
    incomesByDescription: IncomeByDescription[][]
}

export class IncomeAnalysticUseCase implements IUsecase<RequestIncomAnalystic, IncomeAnalysticResponse> {
    private recordRepo: Repository<Record>
    private transactionRepo: Repository<Transaction>

    constructor(
        recordRepo: Repository<Record>,
        transactionRepo: Repository<Transaction>
    ) {
        this.recordRepo = recordRepo
        this.transactionRepo = transactionRepo
    }

    async execute(request: RequestIncomAnalystic): Promise<IncomeAnalysticResponse> {
        const period = mapperPeriod(request.period)
        if (period === Period.DAY || (period === Period.WEEK && request.periodTime === 1))
            throw new UnExpectedError("BAD_PERIOD_SELECTION")

        if (request.showNumber <= 0)
            throw new UnExpectedError("SHOWNUMBER_MUST_BE_GREATER_THAN_)")

        const today = new Date(Date.now()) 

        const incomes: number[] = []
        const incomesByDesc: IncomeByDescription[][] = []  

        for(let i = request.showNumber; i > 0; i--)
        {
            const beginDate = MomentDateService.getUTCDateSubstraction(today, period, 
                (request.periodTime * (i - 1))) 

            const { startDate, endDate } = MomentDateService.getUTCDateByPeriod(beginDate, period, request.periodTime)
            const extendFilter = new TransactionFilter()
            extendFilter.startDate = startDate
            extendFilter.endDate = endDate
            extendFilter.types = [TransactionType.INCOME]
            const transactions = await this.transactionRepo.getAll({limit: 0, offset: 0, queryAll: true}, extendFilter)
            const recordExtendFilter = new RecordFilter()
            recordExtendFilter.transactionIds = transactions.items.map(i => i.getId())
            let records = await this.recordRepo.getAll({offset: 0, limit: 0, queryAll: true}, recordExtendFilter)
            const totalAmount = records.items.reduce((acc: number, i) => acc + i.getMoney().getAmount(), 0)

            const labelIcomes: string[] = []
            records.items.map(i => i.getDescription()).forEach(i => { 
                if (!labelIcomes.includes(i)) // Use string nearst algorithm
                    labelIcomes.push(i) 
            })

            const incomeByDescs: IncomeByDescription[] = []
            labelIcomes.forEach(label => {
                const recordByLabels = records.items.filter(i => i.getDescription() === label)
                const totalAmountBy = recordByLabels.reduce((acc: number, i) => acc + i.getMoney().getAmount(), 0)

                incomeByDescs.push({
                    label: label,
                    income: totalAmountBy 
                })
            })

            incomes.push(totalAmount)
            incomesByDesc.push(incomeByDescs)
        }

        return {
            incomes: incomes,
            incomesByDescription: incomesByDesc
        }
    }
}
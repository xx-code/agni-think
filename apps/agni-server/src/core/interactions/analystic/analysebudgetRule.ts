import { IUsecase } from "../interfaces";
import { MomentDateService } from "@core/domains/entities/libs";
import { AccountType, mapperPeriod, Period, RecordType, SAVING_CATEGORY_ID, TransactionType } from "@core/domains/constants";
import Repository, { TransactionFilter } from "@core/adapters/repository";
import { Transaction } from "@core/domains/entities/transaction";
import { Record } from "@core/domains/entities/record";
import { Account } from "@core/domains/entities/account";
import UnExpectedError from "@core/errors/unExpectedError";

export type RequestAnalyseBudgetRule = {
    period: string
    periodTime: number
    showNumber: number
}

export type BudgetRuleDto = {
    value: number
    transactionType: string
}

export type AnalyseBudgetResponse = {
    distributionSpends: BudgetRuleDto[][]
}

export class AnalyseBudgetRuleUseCase implements IUsecase<RequestAnalyseBudgetRule, AnalyseBudgetResponse> {
    private transactionRepo: Repository<Transaction>
    private recordRepo: Repository<Record>
    private accountRepo: Repository<Account>

    constructor(
        transactionRepo: Repository<Transaction>, 
        recordRepo: Repository<Record>,
        accountRepo: Repository<Account>
    ) {
        this.transactionRepo = transactionRepo
        this.recordRepo = recordRepo
        this.accountRepo = accountRepo
    }

    async execute(request: RequestAnalyseBudgetRule): Promise<AnalyseBudgetResponse> {
        const today = new Date();
        const period = mapperPeriod(request.period)
        if (period === Period.DAY || (period === Period.WEEK && request.periodTime === 1))
            throw new UnExpectedError("BAD_PERIOD_SELECTION")

        if (request.showNumber <= 0)
            throw new UnExpectedError("SHOWNUMBER_MUST_BE_GREATER_THAN_)")

        let results: BudgetRuleDto[][] = []

        for(let i = request.showNumber; i > 0; i--) {
            const beginDate = MomentDateService.getUTCDateSubstraction(today, period, 
                (request.periodTime * (i - 1))) 

            const { startDate, endDate } = MomentDateService.getUTCDateByPeriod(beginDate, period, request.periodTime)
            const extendFilter = new TransactionFilter()
            extendFilter.startDate = startDate
            extendFilter.endDate = endDate
            const transactions = await this.transactionRepo.getAll({
                limit: 0, offset: 0,
                queryAll: true 
            }, extendFilter)

            const records = await this.recordRepo.getManyByIds(transactions.items.map(i => i.getRecordRef()))
            const accounts = await this.accountRepo.getAll({
                limit: 0, offset: 0, queryAll: true
            });

            const transactionTypes = [TransactionType.FIXEDCOST, TransactionType.VARIABLECOST] 
            const results: BudgetRuleDto[] = []
            
            const incomeTransactions = transactions.items.filter(i => i.getTransactionType() === TransactionType.INCOME).map(i => i.getRecordRef())
            const recordByIncomes = records.filter(i => incomeTransactions.includes(i.getId()))
            const incomeAmount = recordByIncomes.reduce((acc: number, record) => acc + record.getMoney().getAmount(), 0)
            
            transactionTypes.forEach(type => {
                const transactionByTypes = transactions.items.filter(i => i.getTransactionType() === type).map(i => i.getRecordRef())
                const recordByTypes = records.filter(i => transactionByTypes.includes(i.getId()) && i.getType() === RecordType.DEBIT)
                const amount = recordByTypes.reduce((acc: number, record) => acc + record.getMoney().getAmount(), 0)
                results.push({
                    transactionType: type ,
                    value: amount // Number(((amount * 100) / incomeAmount).toFixed(2))
                })
            })

            // SAVING part
            const savingAccountIds = accounts.items.filter(i => i.getType() === AccountType.BROKING || i.getType() === AccountType.SAVING)
            .map(i => i.getId())

            const savingTransactions = transactions.items.filter(i => savingAccountIds.includes(i.getAccountRef())).map(i => i.getRecordRef())
            const recordBySaves = records.filter(i => savingTransactions.includes(i.getId()))
            const amountSave = recordBySaves.reduce((acc: number, record) => acc + record.getMoney().getAmount(), 0)

            results.push({
                transactionType: 'Saving long-term',
                value: amountSave // incomeAmount > 0 ? Number(((amountSave * 100) / incomeAmount).toFixed(2)) : 0
            })

            const savingShortTermTrans = transactions.items.filter(i => i.getCategoryRef() === SAVING_CATEGORY_ID).map(i => i.getRecordRef())
            const recordByShortSaves = records.filter(i => savingShortTermTrans.includes(i.getId()) && i.getType() === RecordType.DEBIT)
            const amountShortSave = recordByShortSaves.reduce((acc: number, record) => acc + record.getMoney().getAmount(), 0)

            results.push({
                transactionType: 'Saving short-term',
                value: amountShortSave // incomeAmount > 0 ? Number(((amountShortSave * 100) / incomeAmount).toFixed(2)) : 0
            })

        }

        return { distributionSpends: results }
    }
}
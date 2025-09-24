import { IUsecase } from "../interfaces";
import { MomentDateService } from "@core/domains/entities/libs";
import { AccountType, mapperPeriod, RecordType, SAVING_CATEGORY_ID, TransactionType } from "@core/domains/constants";
import Repository, { TransactionFilter } from "@core/adapters/repository";
import { Transaction } from "@core/domains/entities/transaction";
import { Record } from "@core/domains/entities/record";
import { Account } from "@core/domains/entities/account";

export type RequestAnalyseBudgetRule = {
    period: string
    periodTime: number
    beginDate?: Date
}

export type AnalyseBudgetRuleDto = {
    value: number
    transactionType: string
}

export class AnalyseBudgetRuleUseCase implements IUsecase<RequestAnalyseBudgetRule, AnalyseBudgetRuleDto[]> {
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

    async execute(request: RequestAnalyseBudgetRule): Promise<AnalyseBudgetRuleDto[]> {
        const today = new Date();
        const period = mapperPeriod(request.period)

        const beginDate = request.beginDate || MomentDateService.getUTCDateSubstraction(today, period, request.periodTime) 

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
        const results: AnalyseBudgetRuleDto[] = []
        
        const incomeTransactions = transactions.items.filter(i => i.getTransactionType() === TransactionType.INCOME).map(i => i.getRecordRef())
        const recordByIncomes = records.filter(i => incomeTransactions.includes(i.getId()))
        const incomeAmount = recordByIncomes.reduce((acc: number, record) => acc + record.getMoney().getAmount(), 0)
        
        transactionTypes.forEach(type => {
            const transactionByTypes = transactions.items.filter(i => i.getTransactionType() === type).map(i => i.getRecordRef())
            const recordByTypes = records.filter(i => transactionByTypes.includes(i.getId()) && i.getType() === RecordType.DEBIT)
            const amount = recordByTypes.reduce((acc: number, record) => acc + record.getMoney().getAmount(), 0)
            results.push({
                transactionType: type ,
                value: Number(((amount * 100) / incomeAmount).toFixed(2))
            })
        })

        // SAVING part
        const savingAccountIds = accounts.items.filter(i => i.getType() === AccountType.BROKING || i.getType() === AccountType.SAVING)
        .map(i => i.getId())

        const savingTransactions = transactions.items.filter(i => savingAccountIds.includes(i.getAccountRef())).map(i => i.getRecordRef())
        const recordBySaves = records.filter(i => savingTransactions.includes(i.getId()) && i.getType() === RecordType.CREDIT)
        const amountSave = recordBySaves.reduce((acc: number, record) => acc + record.getMoney().getAmount(), 0)

        results.push({
            transactionType: 'Saving long-term',
            value: Number(((amountSave * 100) / incomeAmount).toFixed(2))
        })

        const savingShortTermTrans = transactions.items.filter(i => i.getCategoryRef() === SAVING_CATEGORY_ID).map(i => i.getRecordRef())
        const recordByShortSaves = records.filter(i => savingShortTermTrans.includes(i.getId()) && i.getType() === RecordType.DEBIT)
        const amountShortSave = recordByShortSaves.reduce((acc: number, record) => acc + record.getMoney().getAmount(), 0)

        results.push({
            transactionType: 'Saving short-term',
            value: Number(((amountShortSave * 100) / incomeAmount).toFixed(2))
        })

        return results
    }
}
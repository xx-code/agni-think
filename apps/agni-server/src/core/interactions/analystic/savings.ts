import { AccountType, mapperPeriod, Period, RecordType, SAVING_CATEGORY_ID, TransactionType, TRANSFERT_CATEGORY_ID } from "@core/domains/constants";
import { IUsecase } from "../interfaces";
import UnExpectedError from "@core/errors/unExpectedError";
import Repository, { TransactionFilter } from "@core/adapters/repository";
import { Transaction } from "@core/domains/entities/transaction";
import { Account } from "@core/domains/entities/account";
import { Record } from "@core/domains/entities/record";
import { MomentDateService } from "@core/domains/entities/libs";

export type RequestSavingAnalystic = {
    period: string
    periodTime: number
    showNumber: number
}

export type SavingAnalysticResponse = {
    savings: number[]
    investements: number[]
    savingRates: number[] 
    investementRates: number[]
}

export class SavingAnalysticUseCase implements IUsecase<RequestSavingAnalystic, SavingAnalysticResponse> {
    private transctionRepo: Repository<Transaction>
    private accountRepo: Repository<Account>
    private recordRepo: Repository<Record>

    constructor(
        transctionRepo: Repository<Transaction>,
        accountRepo: Repository<Account>,
        recordRepo: Repository<Record>
    ) {
        this.transctionRepo = transctionRepo
        this.accountRepo = accountRepo
        this.recordRepo = recordRepo
    }

    async execute(request: RequestSavingAnalystic): Promise<SavingAnalysticResponse> {
        const period = mapperPeriod(request.period)
        if (period === Period.DAY || (period === Period.WEEK && request.periodTime === 1))
            throw new UnExpectedError("BAD_PERIOD_SELECTION")

        if (request.showNumber <= 0)
            throw new UnExpectedError("SHOWNUMBER_MUST_BE_GREATER_THAN_)")

        const accounts = await this.accountRepo.getAll({offset: 0, limit: 0, queryAll: true})
        const saveAccountIds = accounts.items
            .filter(i => i.getType() === AccountType.CHECKING)
            .map(i => i.getId())

        const investAccountIds = accounts.items
            .filter(i => i.getType() === AccountType.BROKING)
            .map(i => i.getId())

        const today = new Date(Date.now()) 

        const savings: number[] = []
        const investements: number[] = []
        const savingRates: number[] = []
        const investementRates: number[] = []
            
        for(let i = request.showNumber; i > 0; i--) {
            const beginDate = MomentDateService.getUTCDateSubstraction(today, period, 
                (request.periodTime * (i - 1))) 

            const { startDate, endDate } = MomentDateService.getUTCDateByPeriod(beginDate, period, request.periodTime)
            const extendFilter = new TransactionFilter()
            extendFilter.startDate = startDate
            extendFilter.endDate = endDate
            extendFilter.categories = [SAVING_CATEGORY_ID,  TRANSFERT_CATEGORY_ID]
            extendFilter.types = [TransactionType.INCOME, TransactionType.OTHER]
            const transactions = await this.transctionRepo.getAll({limit: 0, offset: 0, queryAll: true}, extendFilter)
            

            // const savegoalTransactions = transactions.items.filter(i => i.getCategoryRef() === SAVING_CATEGORY_ID)
            const savingTransactions = transactions.items.filter(i => i.getCategoryRef() === SAVING_CATEGORY_ID || saveAccountIds.includes(i.getAccountRef()))
            const investTransactions = transactions.items.filter(i => investAccountIds.includes(i.getAccountRef()))
            const incomeTransactions = transactions.items.filter(i => i.getRecordRef())

            const incomeRecords = await this.recordRepo.getManyByIds(incomeTransactions.map(i => i.getRecordRef()))
            const incomeAmount = incomeRecords.reduce((acc: number, i) => acc + i.getMoney().getAmount(), 0)

            const savingRecords = await this.recordRepo.getManyByIds(savingTransactions.map(i => i.getRecordRef()))
            const savingAmount = savingRecords.filter(i => i.getType() == RecordType.CREDIT).reduce((acc: number, i) => acc + i.getMoney().getAmount(), 0)

            const investRecords = await this.recordRepo.getManyByIds(investTransactions.map(i => i.getRecordRef()))
            const investAmount = investRecords.filter(i => i.getType() == RecordType.CREDIT).reduce((acc: number, i) => acc + i.getMoney().getAmount(), 0)
            
            savings.push(savingAmount + investAmount)
            investements.push(investAmount)
            savingRates.push(incomeAmount > 0 ? savingAmount/incomeAmount : 0)
            investementRates.push(incomeAmount > 0 ? savingAmount/investAmount : 0)
        }

        return {
            savingRates: savingRates,
            savings : savings,
            investementRates: investementRates,
            investements: investements
        }
    }
}
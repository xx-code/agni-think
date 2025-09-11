import { IUsecase } from "../interfaces"
import { MomentDateService } from "@core/domains/entities/libs"
import { AccountType, RecordType, TransactionType } from "@core/domains/constants"
import Repository, { TransactionFilter } from "@core/adapters/repository"
import { Budget } from "@core/domains/entities/budget"
import { Transaction } from "@core/domains/entities/transaction"
import { Account } from "@core/domains/entities/account"
import { ScheduleTransaction } from "@core/domains/entities/scheduleTransaction"
import { Record } from "@core/domains/entities/record"

export type SuggestPlanningWishSpend = {
    amount: number
    description: string
}

export type SuggestGoalPlanning = {
    saveGoalId: string
    title: string
    amountSuggest: number
    reason: string
}

export type RequestEstimationLeftAmount = {
    startDate: Date
    endDate: Date
}

export type GetEstimationLeftAmoutDto = {
    estimateAmount: number
    balanceScheduleIncome: number
    balanceScheduleSpend: number
    balanceFreezeSchedule: number
    balanceFreeze: number
    balanceBudget: number
} 

export class EstimationLeftAmountUseCase implements IUsecase<RequestEstimationLeftAmount, GetEstimationLeftAmoutDto> {
    private budgetRepo: Repository<Budget>
    private transactionRepo: Repository<Transaction>
    private accountRepo: Repository<Account>
    private scheduleTransactionRepo: Repository<ScheduleTransaction>
    private recordRepository: Repository<Record>

    constructor(
    budgetRepo: Repository<Budget>,
    transationRepo: Repository<Transaction>,
    accountRepo: Repository<Account>,
    recordRepository: Repository<Record>,
    scheduleTransactionRepo: Repository<ScheduleTransaction>) {
        this.budgetRepo = budgetRepo
        this.transactionRepo = transationRepo
        this.accountRepo = accountRepo
        this.recordRepository = recordRepository
        this.scheduleTransactionRepo = scheduleTransactionRepo
    }

    async execute(request: RequestEstimationLeftAmount): Promise<GetEstimationLeftAmoutDto> {
        const scheduleTransactions = await this.scheduleTransactionRepo.getAll({
            limit: 0,
            offset: 0,
            queryAll:true
        });
        const extenFilter = new TransactionFilter()
        extenFilter.isFreeze = true
        extenFilter.startDate = request.startDate
        extenFilter.endDate = request.endDate
        const freezeTransactions = await this.transactionRepo.getAll({ 
            queryAll: true,
            offset: 0,
            limit: 0
        }, extenFilter);
        const accounts = await this.accountRepo.getAll({
            limit: 0,
            offset: 0,
            queryAll: true
        });
        const budgets = await this.budgetRepo.getAll({
            limit: 0,
            offset: 0,
            queryAll: true
        });

        // Compute estimation money
        let amountFreezeTransction = 0
        for(const freeze of freezeTransactions.items) {
            const record  = await this.recordRepository.get(freeze.getRecordRef());
            if (record)
                amountFreezeTransction += record?.getMoney().getAmount()
        }

        let moneyToAllocate = 0
        let income = 0
        let scheduleFreezeAmount = 0
        let scheduleSpendAmount = 0
        for(const scheduleTrans of scheduleTransactions.items) {
            if (
                MomentDateService.compareDate(scheduleTrans.getSchedule().getUpdatedDate(), request.startDate) >= 0 && 
                MomentDateService.compareDate(scheduleTrans.getSchedule().getUpdatedDate(), request.endDate) <= 0 
            ) {
                if (scheduleTrans.getTransactionType() === TransactionType.INCOME)
                {
                    moneyToAllocate += scheduleTrans.getAmount().getAmount() 
                    income += scheduleTrans.getAmount().getAmount()
                }
                else {
                    if (scheduleTrans.getIsFreeze())
                        scheduleFreezeAmount += scheduleTrans.getAmount().getAmount() 
                    else
                        scheduleSpendAmount += scheduleTrans.getAmount().getAmount() 

                    moneyToAllocate -= scheduleTrans.getAmount().getAmount() 
                } 
            }    
        }

        let previsionBudget = 0
        for(const budget of budgets.items) {
            if (
                MomentDateService.compareDate(budget.getSchedule().getUpdatedDate(), request.startDate) >= 0 && 
                MomentDateService.compareDate(budget.getSchedule().getUpdatedDate(), request.endDate) <= 0 &&
                budget.getIsArchive() === false
            ) {
                let startBudgetUTCDate = budget.getSchedule().getStartedDate(); 
                if (budget.getSchedule().getPeriodTime() !== undefined)
                    startBudgetUTCDate = MomentDateService.getUTCDateSubstraction(
                        budget.getSchedule().getUpdatedDate(), 
                        budget.getSchedule().getPeriod(), 
                        budget.getSchedule().getPeriodTime()!
                ); 

                const extendFilter = new TransactionFilter()
                extenFilter.budgets = [budget.getId()]
                extenFilter.startDate = startBudgetUTCDate
                extendFilter.endDate = budget.getSchedule().getUpdatedDate() 
                let transactions = await this.transactionRepo.getAll({
                    queryAll: true,
                    offset: 0,
                    limit: 0
                });

                let currentBalance = 0
                let records = await this.recordRepository.getManyByIds(transactions.items.map(transaction => transaction.getRecordRef()))
                for (let record of records) {
                    if (record.getType() === RecordType.DEBIT)
                        currentBalance += record.getMoney().getAmount()
                }
                const resteBudget = budget.getTarget() - currentBalance
                if (resteBudget > 0)
                    previsionBudget += resteBudget  
            }
        }

        let currentBalance = 0
        for(const account of accounts.items) {
            if ([AccountType.BROKING, AccountType.SAVING].includes(account.getType())) {
                continue;
            }

            currentBalance += account.getBalance()
        }
        currentBalance += amountFreezeTransction;

        const estimationAmountToAllocate = moneyToAllocate + currentBalance - previsionBudget
       
        return {
            estimateAmount: estimationAmountToAllocate,
            balanceBudget: previsionBudget,
            balanceFreeze: amountFreezeTransction,
            balanceFreezeSchedule: scheduleFreezeAmount,
            balanceScheduleIncome: income,
            balanceScheduleSpend: scheduleSpendAmount 
        }
    } 
}
import { BudgetRepository } from "@core/repositories/budgetRepository"
import { IUsecase } from "../interfaces"
import { TransactionRepository } from "@core/repositories/transactionRepository"
import { AccountRepository } from "@core/repositories/accountRepository"
import { ScheduleTransactionRepository } from "@core/repositories/scheduleTransactionRepository"
import { RecordRepository } from "@core/repositories/recordRepository"
import { MomentDateService } from "@core/domains/entities/libs"
import { AccountType, RecordType, TransactionType } from "@core/domains/constants"

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
    private budgetRepo: BudgetRepository
    private transactionRepo: TransactionRepository
    private accountRepo: AccountRepository
    private scheduleTransactionRepo: ScheduleTransactionRepository
    private recordRepository: RecordRepository

    constructor(
    budgetRepo: BudgetRepository,
    transationRepo: TransactionRepository,
    accountRepo: AccountRepository,
    recordRepository: RecordRepository,
    scheduleTransactionRepo: ScheduleTransactionRepository) {
        this.budgetRepo = budgetRepo
        this.transactionRepo = transationRepo
        this.accountRepo = accountRepo
        this.recordRepository = recordRepository
        this.scheduleTransactionRepo = scheduleTransactionRepo
    }

    async execute(request: RequestEstimationLeftAmount): Promise<GetEstimationLeftAmoutDto> {
        const scheduleTransactions = await this.scheduleTransactionRepo.getAll();
        const freezeTransactions = await this.transactionRepo.getTransactions({ 
            isFreeze: true,
            startDate: request.startDate,
            endDate: request.endDate
        });
        const accounts = await this.accountRepo.getAll();
        const budgets = await this.budgetRepo.getAll();

        // Compute estimation money
        let amountFreezeTransction = 0
        for(const freeze of freezeTransactions) {
            const record  = await this.recordRepository.get(freeze.getRecordRef());
            if (record)
                amountFreezeTransction += record?.getMoney().getAmount()
        }

        let moneyToAllocate = 0
        let income = 0
        let scheduleFreezeAmount = 0
        let scheduleSpendAmount = 0
        for(const scheduleTrans of scheduleTransactions) {
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
        for(const budget of budgets) {
            if (
                MomentDateService.compareDate(budget.getSchedule().getUpdatedDate(), request.startDate) >= 0 && 
                MomentDateService.compareDate(budget.getSchedule().getUpdatedDate(), request.endDate) <= 0 &&
                !budget.getIsArchive()
            ) {
                let startBudgetUTCDate = budget.getSchedule().getStartedDate(); 
                if (budget.getSchedule().getPeriodTime() !== undefined)
                    startBudgetUTCDate = MomentDateService.getUTCDateSubstraction(
                        budget.getSchedule().getUpdatedDate(), 
                        budget.getSchedule().getPeriod(), 
                        budget.getSchedule().getPeriodTime()!
                ); 

                let transactions = await this.transactionRepo.getTransactions({
                    budgets: [budget.getId()],
                    startDate: startBudgetUTCDate,
                    endDate: budget.getSchedule().getUpdatedDate(),
                });

                let currentBalance = 0
                let records = await this.recordRepository.getManyById(transactions.map(transaction => transaction.getRecordRef()))
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
        for(const account of accounts) {
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
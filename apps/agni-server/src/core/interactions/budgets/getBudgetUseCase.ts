import { BudgetRepository } from "../../repositories/budgetRepository";
import { TransactionRepository } from "../../repositories/transactionRepository";
import { RecordRepository } from "@core/repositories/recordRepository";
import { IUsecase } from "../interfaces";
import { RecordType } from "@core/domains/constants";

export type GetBudgetDto = {
    id: string,
    title: string,
    target: number,
    period: string
    periodTime?: number
    currentBalance: number
    startDate: string
    updateDate: string
    endDate?: string
}

export class GetBudgetUseCase implements IUsecase<string, GetBudgetDto> {
   private budgetRepository: BudgetRepository;
   private transactionRepository: TransactionRepository;
   private recordRepository: RecordRepository

   constructor(
    budgetRepository: BudgetRepository,
    transactionRepository: TransactionRepository,
    recordRepository: RecordRepository
   ) {
       this.budgetRepository = budgetRepository
       this.recordRepository = recordRepository
       this.transactionRepository = transactionRepository
   }


    async execute(id: string): Promise<GetBudgetDto> {
        let budget = await this.budgetRepository.get(id);
           
        let transactions = await this.transactionRepository.getTransactions({
            categories: [],
            accounts: [],
            tags: [],
            budgets: [budget.getId()],
            types: [],
            startDate: budget.getSchedule().getStartedDate().toLocaleDateString(),
            endDate: budget.getSchedule().getStartedDate().toLocaleDateString() 
        });

        let currentBalance = 0
        let records = await this.recordRepository.getManyById(transactions.map(transaction => transaction.getRecordRef()))
        for (let record of records) {
            if (record.getType() === RecordType.DEBIT)
                currentBalance += record.getMoney().getAmount()
        }
        
        let budgetDisplay: GetBudgetDto = {
            id: budget.getId(),
            title: budget.getTitle(),
            currentBalance: currentBalance,
            period: budget.getSchedule().getPeriod(),
            periodTime: budget.getSchedule().getPeriodTime(),
            target: budget.getTarget(),
            startDate: budget.getSchedule().getStartedDate().toLocaleString(),
            updateDate: budget.getSchedule().getUpdatedDate().toLocaleString(),
            endDate: budget.getSchedule().getEndingDate()?.toLocaleString()
        };

        return budgetDisplay
    
   }
}

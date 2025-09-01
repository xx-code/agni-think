import { BudgetRepository } from "../../repositories/budgetRepository";
import { TransactionRepository } from "../../repositories/transactionRepository";
import { RecordRepository } from "@core/repositories/recordRepository";
import { IUsecase } from "../interfaces";
import { ListDto, QueryAllFetch } from "@core/dto/base";
import { RecordType } from "@core/domains/constants";
import { MomentDateService } from "@core/domains/entities/libs";

export type GetAllBudgetDto = {
    id: string,
    title: string,
    target: number,
    period: string
    periodTime?: number
    currentBalance: number
    startDate: Date
    updateDate: Date
    endDate?: Date
}

export class GetAllBudgetUseCase implements IUsecase<QueryAllFetch, ListDto<GetAllBudgetDto>> {
   private budgetRepository: BudgetRepository;
   private transactionRepository: TransactionRepository;
   private recordRepository: RecordRepository
  
   constructor(
    budgetRepository: BudgetRepository,
    transactionRepository: TransactionRepository,
    recordRepository: RecordRepository
   ) {
       this.budgetRepository = budgetRepository
       this.transactionRepository = transactionRepository
       this.recordRepository = recordRepository
   }


   async execute(request: QueryAllFetch): Promise<ListDto<GetAllBudgetDto>> {
        let budgets = await this.budgetRepository.getAll({
            limit: request.limit,
            offset: request.offset,
            queryAll: request.queryAll
        });
    
        let budgetsDisplay = []

        for (let i = 0; i < budgets.items.length; i++) {
            let budget = budgets.items[i];
            if (budget.getIsArchive())
                continue;

            let startBudgetUTCDate = budget.getSchedule().getStartedDate(); 
            if (budget.getSchedule().getPeriodTime() !== undefined)
                startBudgetUTCDate = MomentDateService.getUTCDateSubstraction(
                    budget.getSchedule().getUpdatedDate(), 
                    budget.getSchedule().getPeriod(), 
                    budget.getSchedule().getPeriodTime()!
            ); 

            let transactions = await this.transactionRepository.getTransactions({
                budgets: [budget.getId()],
                startDate: startBudgetUTCDate,
                endDate: budget.getSchedule().getUpdatedDate(),
                offset: 0,
                limit: 0,
                queryAll: true
            });

            let currentBalance = 0
            let records = await this.recordRepository.getManyById(transactions.map(transaction => transaction.getRecordRef()))
            for (let record of records) {
                if (record.getType() === RecordType.DEBIT)
                    currentBalance += record.getMoney().getAmount()
            }

            let budgetDisplay: GetAllBudgetDto = {
                id: budget.getId(),
                title: budget.getTitle(),
                currentBalance: currentBalance,
                period: budget.getSchedule().getPeriod(),
                periodTime: budget.getSchedule().getPeriodTime(),
                target: budget.getTarget(),
                startDate: budget.getSchedule().getStartedDate(),
                updateDate: budget.getSchedule().getUpdatedDate(),
                endDate: budget.getSchedule().getEndingDate()
            };

            budgetsDisplay.push(budgetDisplay);
        }
        return { items: budgetsDisplay, totals: budgets.total }
   }
}

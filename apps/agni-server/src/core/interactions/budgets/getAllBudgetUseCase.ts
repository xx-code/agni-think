import { IUsecase } from "../interfaces";
import { ListDto, QueryFilter } from "@core/dto/base";
import { RecordType } from "@core/domains/constants";
import { MomentDateService } from "@core/domains/entities/libs";
import Repository, { TransactionFilter } from "@core/adapters/repository";
import { Budget } from "@core/domains/entities/budget";
import { Transaction } from "@core/domains/entities/transaction";
import { Record } from "@core/domains/entities/record";
import { SaveGoal } from "@core/domains/entities/saveGoal";

export type GetAllBudgetDto = {
    id: string,
    title: string,
    target: number,
    realTarget: number,
    saveGoalTarget: number,
    saveGoalIds: string[]
    period: string
    periodTime?: number
    currentBalance: number
    startDate: Date
    updateDate: Date
    endDate?: Date
}

export class GetAllBudgetUseCase implements IUsecase<QueryFilter, ListDto<GetAllBudgetDto>> {
   private budgetRepository: Repository<Budget>;
   private transactionRepository: Repository<Transaction>;
   private recordRepository: Repository<Record>
   private saveGoalRepository: Repository<SaveGoal>
  
   constructor(
    budgetRepository: Repository<Budget>,
    transactionRepository: Repository<Transaction>,
    recordRepository: Repository<Record>,
    saveGoalRepository: Repository<SaveGoal>
   ) {
       this.budgetRepository = budgetRepository
       this.transactionRepository = transactionRepository
       this.recordRepository = recordRepository
       this.saveGoalRepository = saveGoalRepository
   }


   async execute(request: QueryFilter): Promise<ListDto<GetAllBudgetDto>> {
        let budgets = await this.budgetRepository.getAll({
            limit: request.limit,
            offset: request.offset,
            sort: request.sortBy ? {
                sortBy: request.sortBy,
                asc: request.sortSense === 'asc'
            } : undefined,
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

            const extendFilter = new TransactionFilter()
            extendFilter.budgets = [budget.getId()]
            extendFilter.startDate = startBudgetUTCDate
            extendFilter.endDate = budget.getSchedule().getUpdatedDate()
            let transactions = await this.transactionRepository.getAll({
                offset: 0,
                limit: 0,
                queryAll: true
            }, extendFilter);

            let currentBalance = 0
            let records = await this.recordRepository.getManyByIds(transactions.items.map(transaction => transaction.getRecordRef()))
            for (let record of records) {
                if (record.getType() === RecordType.DEBIT)
                    currentBalance += record.getMoney().getAmount()
            }

            let saveGoals = await this.saveGoalRepository.getManyByIds(budget.getSaveGoalIds())
            let saveBalance = 0
            for (let saveGoal of saveGoals) 
                saveBalance += saveGoal.getBalance().getAmount() 

            let budgetDisplay: GetAllBudgetDto = {
                id: budget.getId(),
                title: budget.getTitle(),
                currentBalance: currentBalance,
                period: budget.getSchedule().getPeriod(),
                periodTime: budget.getSchedule().getPeriodTime(),
                target: budget.getTarget() + saveBalance,
                saveGoalTarget: saveBalance,
                saveGoalIds: budget.getSaveGoalIds(),
                realTarget: budget.getTarget(),
                startDate: budget.getSchedule().getStartedDate(),
                updateDate: budget.getSchedule().getUpdatedDate(),
                endDate: budget.getSchedule().getEndingDate()
            };

            budgetsDisplay.push(budgetDisplay);
        }
        return { items: budgetsDisplay, totals: budgets.total }
   }
}

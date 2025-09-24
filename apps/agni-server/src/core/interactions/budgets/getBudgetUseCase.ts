import Repository, { TransactionFilter } from "@core/adapters/repository";
import { IUsecase } from "../interfaces";
import { RecordType } from "@core/domains/constants";
import { MomentDateService } from "@core/domains/entities/libs";
import { Budget } from "@core/domains/entities/budget";
import { Transaction } from "@core/domains/entities/transaction";
import { Record } from "@core/domains/entities/record";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { SaveGoal } from "@core/domains/entities/saveGoal";

export type GetBudgetDto = {
    id: string,
    title: string,
    target: number,
    realTarget: number,
    saveGoalTarget: number
    saveGoalIds: string[]
    period: string
    periodTime?: number
    currentBalance: number
    startDate: Date
    updateDate: Date
    endDate?: Date
}

export class GetBudgetUseCase implements IUsecase<string, GetBudgetDto> {
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
       this.recordRepository = recordRepository
       this.transactionRepository = transactionRepository
       this.saveGoalRepository = saveGoalRepository
   }


    async execute(id: string): Promise<GetBudgetDto> {
        let budget = await this.budgetRepository.get(id);
        if (!budget)
            throw new ResourceNotFoundError("BUDGET_NOT_FOUND")
           
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
            limit: 0, 
            offset: 0,
            queryAll: true,
        }, extendFilter);

        let currentBalance = 0
        let records = await this.recordRepository.getManyByIds(transactions.items.map(transaction => transaction.getRecordRef()))
        for (let record of records) {
            if (record.getType() === RecordType.DEBIT)
                currentBalance += record.getMoney().getAmount()
        }

        let saveBalance = 0
        let saveGoals = await this.saveGoalRepository.getManyByIds(budget.getSaveGoalIds())
        for (let saveGoal of saveGoals) 
            saveBalance += saveGoal.getBalance().getAmount()
        
        let budgetDisplay: GetBudgetDto = {
            id: budget.getId(),
            title: budget.getTitle(),
            currentBalance: currentBalance,
            period: budget.getSchedule().getPeriod(),
            periodTime: budget.getSchedule().getPeriodTime(),
            target: budget.getTarget() + saveBalance,
            realTarget: budget.getTarget(),
            saveGoalIds: budget.getSaveGoalIds(),
            saveGoalTarget: saveBalance,
            startDate: budget.getSchedule().getStartedDate(),
            updateDate: budget.getSchedule().getUpdatedDate(),
            endDate: budget.getSchedule().getEndingDate()
        };

        return budgetDisplay
    
   }
}

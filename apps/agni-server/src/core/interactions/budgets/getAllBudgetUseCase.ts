import { IUsecase } from "../interfaces";
import { ListDto, QueryFilter } from "@core/dto/base";
import { DeductionBase, DeductionMode, RecordType, TransactionStatus, TransactionType } from "@core/domains/constants";
import { MomentDateService } from "@core/domains/entities/libs";
import Repository, { RecordFilter, TransactionFilter } from "@core/adapters/repository";
import { Budget } from "@core/domains/entities/budget";
import { Transaction } from "@core/domains/entities/transaction";
import { Record } from "@core/domains/entities/record";
import { SaveGoal } from "@core/domains/entities/saveGoal";
import { DeductionType } from "@core/domains/entities/decution";
import { RequestGetPagination } from "../transaction/getPaginationTransactionUseCase";
import { GetBalanceDto } from "../transaction/getBalanceByUseCase";

export type GetAllBudgetDto = {
    id: string,
    title: string,
    target: number,
    realTarget: number,
    saveGoalTarget: number,
    saveGoalIds: string[]
    currentBalance: number
    dueDate: Date
    repeater?: {
        period: string
        interval: number
    }
}

export class GetAllBudgetUseCase implements IUsecase<QueryFilter, ListDto<GetAllBudgetDto>> {
    private budgetRepository: Repository<Budget>;
    private transactionRepository: Repository<Transaction>;
    private recordRepository: Repository<Record>
    private saveGoalRepository: Repository<SaveGoal>
    private getBalanceUc: IUsecase<RequestGetPagination, GetBalanceDto>
  
   constructor(
    budgetRepository: Repository<Budget>,
    transactionRepository: Repository<Transaction>,
    recordRepository: Repository<Record>,
    saveGoalRepository: Repository<SaveGoal>,
    deductionRepo: Repository<DeductionType>,
    getBalanceUc: IUsecase<RequestGetPagination, GetBalanceDto>
   ) {
       this.budgetRepository = budgetRepository
       this.transactionRepository = transactionRepository
       this.recordRepository = recordRepository
       this.saveGoalRepository = saveGoalRepository
       this.getBalanceUc = getBalanceUc
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

            let startBudgetUTCDate = budget.getSchedule().dueDate 
            if (budget.getSchedule().dueDate !== undefined)
                startBudgetUTCDate = MomentDateService.getUTCDateSubstraction(
                    budget.getSchedule().dueDate, 
                    budget.getSchedule().repeater!.period, 
                    budget.getSchedule().repeater!.interval
            ); 

            const res = await this.getBalanceUc.execute({ 
                limit: 0, offset: 0, 
                budgetFilterIds: [budget.getId()],
                types: [TransactionType.FIXEDCOST, TransactionType.VARIABLECOST, TransactionType.OTHER],
                status: TransactionStatus.COMPLETE,
                dateStart: startBudgetUTCDate,
                dateEnd: budget.getSchedule().dueDate ,
            })

        let currentBalance = res.balance

            let saveGoals = await this.saveGoalRepository.getManyByIds(budget.getSaveGoalIds())
            let saveBalance = 0
            for (let saveGoal of saveGoals) 
                saveBalance += saveGoal.getBalance().getAmount() 

            let budgetDisplay: GetAllBudgetDto = {
                id: budget.getId(),
                title: budget.getTitle(),
                currentBalance: Math.abs(currentBalance),
                target: budget.getTarget() + saveBalance,
                saveGoalTarget: saveBalance,
                saveGoalIds: budget.getSaveGoalIds(),
                realTarget: budget.getTarget(),
                dueDate: budget.getSchedule().dueDate,
                repeater: budget.getSchedule(). repeater ? {
                    period: budget.getSchedule().repeater!.period,
                    interval: budget.getSchedule().repeater!.interval
                } : undefined 
            };

            budgetsDisplay.push(budgetDisplay);
        }
        return { items: budgetsDisplay, totals: budgets.total }
   }
}

import { mapperPeriod } from "@core/domains/constants";
import { IUsecase } from "../interfaces";
import { Scheduler } from "@core/domains/valueObjects/scheduleInfo";
import { Budget } from "@core/domains/entities/budget";
import Repository from "@core/adapters/repository";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { SaveGoal } from "@core/domains/entities/saveGoal";

export type RequestCreateBudgetSchedule = {
    repeater?: {
        period: string
        interval: number
    }
    dueDate: Date
}

export type RequestUpdateBudget = {
    id: string
    title?: string;
    target?: number;
    schedule?: RequestCreateBudgetSchedule 
    saveGoalIds?: string[]
} 

export class UpdateBudgetUseCase implements IUsecase<RequestUpdateBudget, void> {
    private budgetRepository: Repository<Budget>;
    private saveGoalRepository: Repository<SaveGoal>;

   constructor(
    repo: Repository<Budget>,
    saveGoalRepository: Repository<SaveGoal>) {
    this.budgetRepository = repo
    this.saveGoalRepository = saveGoalRepository
   }

   async execute(request: RequestUpdateBudget): Promise<void> {
    let budget = await this.budgetRepository.get(request.id)
    if (!budget)
        throw new ResourceNotFoundError("BUDGET_NOT_FOUND")
    
    if (request.title)
        budget.setTitle(request.title)
    
    if (request.target)
        budget.setTarget(request.target)

    if (request.saveGoalIds !== undefined && request.saveGoalIds.length > 0) {
        const saveGoals = await this.saveGoalRepository.getManyByIds(request.saveGoalIds) 
        if (saveGoals.length !== request.saveGoalIds.length)
            throw new ResourceNotFoundError("SOME_SAVE_GOAL_FOUND")
        budget.setSaveGoalIds(request.saveGoalIds)
    }

    if (request.schedule) {
        const scheduler = new Scheduler(
            new Date(request.schedule.dueDate),
            request.schedule.repeater ? {
                period: mapperPeriod(request.schedule.repeater.period),
                interval: request.schedule.repeater.interval
            } : undefined
        )
        budget.reSchedule(scheduler)
    }
            
    if (budget.hasChange()) {
        await this.budgetRepository.update(budget);
    }
   }
}

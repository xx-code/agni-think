import { mapperPeriod } from "@core/domains/constants";
import { IUsecase } from "../interfaces";
import { Scheduler } from "@core/domains/valueObjects/scheduleInfo";
import { Budget } from "@core/domains/entities/budget";
import Repository from "@core/adapters/repository";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";

export type RequestCreateBudgetSchedule = {
    period: string;
    periodTime?: number;
    dateStart: Date;
    dateEnd?: Date;
}

export type RequestUpdateBudget = {
    id: string
    title?: string;
    target?: number;
    schedule?: RequestCreateBudgetSchedule 
} 

export class UpdateBudgetUseCase implements IUsecase<RequestUpdateBudget, void> {
    private budgetRepository: Repository<Budget>;

   constructor(repo: Repository<Budget>) {
    this.budgetRepository = repo
   }

   async execute(request: RequestUpdateBudget): Promise<void> {
    let budget = await this.budgetRepository.get(request.id)
    if (!budget)
        throw new ResourceNotFoundError("BUDGET_NOT_FOUND")
    
    if (request.title)
        budget.setTitle(request.title)
    
    if (request.target)
        budget.setTarget(request.target)

    if (request.schedule) {
        const scheduler = new Scheduler(
            mapperPeriod(request.schedule.period),
            request.schedule.dateStart ,
            request.schedule.periodTime,
            request.schedule.dateEnd
        )
        budget.reSchedule(scheduler)
    }
            
    if (budget.hasChange())
        await this.budgetRepository.update(budget);
   }
}

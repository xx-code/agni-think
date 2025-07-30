import { BudgetRepository } from "../../repositories/budgetRepository";
import { mapperPeriod } from "@core/domains/constants";
import { IUsecase } from "../interfaces";
import { Scheduler } from "@core/domains/valueObjects/scheduleInfo";
import { MomentDateService } from "@core/domains/entities/libs";

export type RequestCreateBudgetSchedule = {
    period: string;
    periodTime?: number;
    dateStart: string;
    dateEnd?: string;
}

export type RequestUpdateBudget = {
    id: string
    title: string;
    target: number;
    schedule: RequestCreateBudgetSchedule 
} 

export class UpdateBudgetUseCase implements IUsecase<RequestUpdateBudget, void> {
    private budgetRepository: BudgetRepository;

   constructor(repo: BudgetRepository) {
    this.budgetRepository = repo
   }

   async execute(request: RequestUpdateBudget): Promise<void> {
    let budget = await this.budgetRepository.get(request.id)
    
    budget.setTitle(request.title)
    budget.setTarget(request.target)

    const scheduler = new Scheduler(
        mapperPeriod(request.schedule.period),
        MomentDateService.formatDate(request.schedule.dateStart) ,
        request.schedule.periodTime,
        request.schedule.dateEnd ? MomentDateService.formatDate(request.schedule.dateEnd) : undefined
    )
    budget.reSchedule(scheduler)
            
    if (budget.hasChange())
        await this.budgetRepository.update(budget);
   }
}

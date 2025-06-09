import { BudgetRepository } from "../../repositories/budgetRepository";
import { TagRepository } from "../../repositories/tagRepository";
import { CategoryRepository } from "../../repositories/categoryRepository";
import { DateService } from "@core/adapters/libs";
import { mapperPeriod } from "@core/domains/constants";
import { isEmpty } from "@core/domains/helpers";
import { ValueError } from "@core/errors/valueError";

export type RequestUpdateBudget = {
   id: string
   title: string
   target: number
   dateStart: string
   dateEnd: string
   period: string
   periodTime: number
}

export interface IUpdateBudgetUseCase {
   execute(request: RequestUpdateBudget): void
}


export interface IUpdateBudgetUseCasePresenter {
   success(success: boolean): void;
   fail(err: Error): void;
}

export interface IUpdateBudgetAdapter { 
    budgetRepository: BudgetRepository
    categoryRepository: CategoryRepository
    tagRepository: TagRepository
    dateService: DateService
}

export class UpdateBudgetUseCase implements IUpdateBudgetUseCase {
   private budgetRepository: BudgetRepository;
   private dateService: DateService
   private presenter: IUpdateBudgetUseCasePresenter;


   constructor(repo: IUpdateBudgetAdapter, presenter: IUpdateBudgetUseCasePresenter) {
       this.budgetRepository = repo.budgetRepository
       this.dateService = repo.dateService
       this.presenter = presenter
   }

   async execute(request: RequestUpdateBudget): Promise<void> {
       try {
        let budget = await this.budgetRepository.get(request.id)
        
        budget.setTitle(request.title)
        budget.setTarget(request.target)

        budget.setDateStart(request.dateStart)
        budget.setDateStart(this.dateService.formatDate(request.dateStart))           
        budget.setTarget(request.target) 
        if (!isEmpty(request.dateEnd))
            budget.setDateEnd(this.dateService.formatDate(request.dateEnd))
        
        if (!isEmpty(request.period)) {
            let period = mapperPeriod(request.period)
            budget.setPeriod(period)
            budget.setPeriodTime(request.periodTime)
        }
        
        if (isEmpty(budget.getDateEnd()) && budget.getPeriod() && budget.getPeriodTime() === 0) {
            throw new ValueError('this type of budget don\'t exit, an budget must have date of end or period count or all in same time')
        } 

        let dateUpdate = budget.getDateEnd()

        if (budget.getPeriod()) {
            if (budget.getPeriodTime() <= 0)
                throw new ValueError("Period not define")

            dateUpdate = this.dateService.getDateAddition!(budget.getDateStart(), 
            budget.getPeriod()!, budget.getPeriodTime())    
        } 

        budget.setDateUpdate(dateUpdate!)
                
        if (budget.hasChange())
            await this.budgetRepository.update(budget);

        this.presenter.success(true);

       } catch(err) {
           this.presenter.fail(err as Error);
       }
   }
}

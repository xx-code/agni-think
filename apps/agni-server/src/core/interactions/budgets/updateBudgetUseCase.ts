import { BudgetRepository } from "../../repositories/budgetRepository";
import { TagRepository } from "../../repositories/tagRepository";
import { CategoryRepository } from "../../repositories/categoryRepository";
import { DateService } from "@core/adapters/libs";
import { mapperPeriod } from "@core/domains/constants";
import { BudgetBuilder } from "@core/domains/entities/budget";
import { isEmpty } from "@core/domains/helpers";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";

export type RequestUpdateBudget = {
   id: string
   title: string
   target: number
   dateStart: string
   dateEnd: string
   period: string
   periodTime: number
   tagsRef: string[]
   categoriesRef: string[]
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
   private categoryRepository: CategoryRepository;
   private tagRepository: TagRepository;
   private dateService: DateService
   private presenter: IUpdateBudgetUseCasePresenter;


   constructor(repo: IUpdateBudgetAdapter, presenter: IUpdateBudgetUseCasePresenter) {
       this.budgetRepository = repo.budgetRepository
       this.tagRepository = repo.tagRepository
       this.categoryRepository = repo.categoryRepository
       this.dateService = repo.dateService
       this.presenter = presenter
   }

   async execute(request: RequestUpdateBudget): Promise<void> {
       try {
           let budget = await this.budgetRepository.get(request.id)

           budget.setTitle(request.title)
           budget.setTarget(request.target)

           budget.setDateStart(request.dateStart)

           if (!(await this.categoryRepository.isCategoryExistByIds(request.categoriesRef))) 
                throw new ResourceNotFoundError("Category not exist")
            
            budget.setCategories(request.categoriesRef)
            
            if (!(await this.tagRepository.isTagExistByIds(request.tagsRef)))
                throw new ResourceNotFoundError("an tag not found")
                
            budget.setTags(request.tagsRef)

            budget.setDateStart(this.dateService.formatDate(request.dateStart))           
            budget.setTarget(request.target) 
            if (!isEmpty(request.dateEnd))
                budget.setDateEnd(this.dateService.formatDate(request.dateEnd))
            
            if (!isEmpty(request.period)) {
                let period = mapperPeriod(request.period)
                budget.setPeriod(period)
                budget.setPeriodTime(request.periodTime)
            } 

            let budgetBuilder = new BudgetBuilder()
            budgetBuilder.setBudget(budget)
            budget = budgetBuilder.getBudget(this.dateService.getDateAddition)! 
                    
            if (budget.hasChange())
                await this.budgetRepository.update(budget);

           this.presenter.success(true);
       } catch(err) {
           this.presenter.fail(err as Error);
       }
   }
}

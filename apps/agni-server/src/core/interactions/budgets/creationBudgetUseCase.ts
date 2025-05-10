import { DateService, GetUID } from "@core/adapters/libs";
import { mapperPeriod } from "@core/domains/constants";
import { BudgetBuilder } from "@core/domains/entities/budget";
import { isEmpty } from "@core/domains/helpers";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { BudgetRepository } from "../../repositories/budgetRepository";
import { CategoryRepository } from "../../repositories/categoryRepository";
import { TagRepository } from "../../repositories/tagRepository";


export type RequestCreationBudgetUseCase = {
    title: string;
    target: number;
    period: string;
    periodTime: number;
    dateStart: string;
    dateEnd: string;
    categoriesRef: string[]
    tagRefs: string[]
} 

export interface ICreationBudgetUseCase {
    execute(request: RequestCreationBudgetUseCase): void;
}

export interface ICreationBudgetUseCaseResponse {
    success(newBudgetId: string): void;
    fail(err: Error): void;
}

export interface ICreationBudgetAdapter {
    budgetRepository: BudgetRepository
    categoryRepository: CategoryRepository
    tagRepository: TagRepository
    dateService: DateService
}

export class CreationBudgetUseCase implements ICreationBudgetUseCase {
    private budgetRepository: BudgetRepository;
    private categoryRepository: CategoryRepository;
    private tagRepository: TagRepository;
    private presenter: ICreationBudgetUseCaseResponse;
    private dateService: DateService

    constructor(adapters: ICreationBudgetAdapter, dateService: DateService, presenter: ICreationBudgetUseCaseResponse) {
        this.budgetRepository = adapters.budgetRepository
        this.categoryRepository = adapters.categoryRepository
        this.tagRepository = adapters.tagRepository
        this.dateService = dateService
        this.presenter = presenter
    }

    async execute(request: RequestCreationBudgetUseCase): Promise<void> {
        try {
            const budgetBuilder = new BudgetBuilder()
 
            if ((await this.budgetRepository.isBudgetExistByName(request.title)))
                throw new ResourceAlreadyExist("Budget already exist")

            budgetBuilder.setTitle(request.title)
            
            if (request.categoriesRef.length > 0 && !(await this.categoryRepository.isCategoryExistByIds(request.categoriesRef))) 
                throw new ResourceNotFoundError("Category not exist")
            
            budgetBuilder.setCategories(request.categoriesRef)
            
            if (request.tagRefs.length > 0 && !(await this.tagRepository.isTagExistByIds(request.tagRefs)))
                throw new ResourceNotFoundError("an tag not found")
              
            budgetBuilder.setTags(request.tagRefs)
 
            budgetBuilder.setDateStart(this.dateService.formatDate(request.dateStart) ) 
            budgetBuilder.setTarget(request.target) 
            if (!isEmpty(request.dateEnd))
                budgetBuilder.setDateEnd(this.dateService.formatDate(request.dateEnd))
           
            if (!isEmpty(request.period)) {
                let period = mapperPeriod(request.period)
                budgetBuilder.setPeriod(period)
                budgetBuilder.setPeriodTime(request.periodTime)
            }  
        
            budgetBuilder.setId(GetUID())
 
            let newBudget = budgetBuilder.getBudget(this.dateService.getDateAddition)
 
            await this.budgetRepository.save(newBudget!);
 
            this.presenter.success(newBudget!.getId());
        } catch(err) {
            this.presenter.fail(err as Error);
        } 
    }
}
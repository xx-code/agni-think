import { DateService, GetUID } from "@core/adapters/libs";
import { mapperPeriod } from "@core/domains/constants";
import { isEmpty } from "@core/domains/helpers";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { BudgetRepository } from "../../repositories/budgetRepository";
import { CategoryRepository } from "../../repositories/categoryRepository";
import { TagRepository } from "../../repositories/tagRepository";
import { Budget } from "@core/domains/entities/budget";
import { ValueError } from "@core/errors/valueError";


export type RequestCreationBudgetUseCase = {
    title: string;
    target: number;
    period: string;
    periodTime: number;
    dateStart: string;
    dateEnd: string;
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
    private presenter: ICreationBudgetUseCaseResponse;
    private dateService: DateService

    constructor(adapters: ICreationBudgetAdapter, dateService: DateService, presenter: ICreationBudgetUseCaseResponse) {
        this.budgetRepository = adapters.budgetRepository
        this.dateService = dateService
        this.presenter = presenter
    }

    async execute(request: RequestCreationBudgetUseCase): Promise<void> {
        try {
 
            if ((await this.budgetRepository.isBudgetExistByName(request.title)))
                throw new ResourceAlreadyExist("Budget already exist")

            const dateStart = this.dateService.formatDate(request.dateStart)
            let dateEnd = null
            if (!isEmpty(request.dateEnd))
                dateEnd = this.dateService.formatDate(request.dateEnd)

            let periodTime = 0
            let period = null 
            if (!isEmpty(request.period)) {
                period = mapperPeriod(request.period)
                periodTime = request.periodTime
            }
            
            if (isEmpty(dateEnd) && period && periodTime === 0) {
                throw new ValueError('this type of budget don\'t exit, an budget must have date of end or period count or all in same time')
            } 

            let dateUpdate = null
            if (!isEmpty(dateEnd)) {
                dateUpdate = dateEnd
            }

            if (period) {
                if (periodTime <= 0)
                    throw new ValueError("Period not define")

                dateUpdate = this.dateService.getDateAddition!(dateStart, period, periodTime)    
            } 

            const newBudget = new Budget(GetUID(), false, request.target, request.title, dateStart, period, 
            periodTime, dateEnd, dateUpdate!); 
        
            await this.budgetRepository.save(newBudget!);
 
            this.presenter.success(newBudget!.getId());
        } catch(err) {
            this.presenter.fail(err as Error);
        } 
    }
}
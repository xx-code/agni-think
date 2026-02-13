import { GetUID } from "@core/adapters/libs";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { Budget } from "@core/domains/entities/budget";
import { IUsecase } from "../interfaces";
import { Scheduler } from "@core/domains/valueObjects/scheduleInfo";
import { mapperPeriod } from "@core/domains/constants";
import { CreatedDto } from "@core/dto/base";
import Repository from "@core/adapters/repository";
import { SaveGoal } from "@core/domains/entities/saveGoal";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";

export type RequestCreateBudgetSchedule = {
    repeater?: {
        period: string
        interval: number
    }
    dueDate: Date
}

export type RequestCreationBudgetUseCase = {
    title: string;
    target: number;
    schedule: RequestCreateBudgetSchedule 
    saveGoalIds: string[]
} 

export class CreationBudgetUseCase implements IUsecase<RequestCreationBudgetUseCase, CreatedDto> {
    private budgetRepository: Repository<Budget>
    private saveGoalRepository: Repository<SaveGoal>

    constructor(
        budgetRepository: Repository<Budget>,
        saveGoalRepository: Repository<SaveGoal>
    ) {
        this.budgetRepository = budgetRepository
        this.saveGoalRepository = saveGoalRepository
    }

    async execute(request: RequestCreationBudgetUseCase): Promise<CreatedDto> {
        if ((await this.budgetRepository.existByName(request.title)))
            throw new ResourceAlreadyExist("BUDGET_ALREADY_EXIST")

        if (request.saveGoalIds.length > 0) {
            const saveGoals = await this.saveGoalRepository.getManyByIds(request.saveGoalIds) 
            if (saveGoals.length !== request.saveGoalIds.length)
                throw new ResourceNotFoundError("SOME_SAVE_GOAL_FOUND")
        }

        const scheduler = new Scheduler(
            new Date(request.schedule.dueDate),
            request.schedule.repeater ? {
                period: mapperPeriod(request.schedule.repeater.period),
                interval: request.schedule.repeater.interval
            } : undefined
        )

        const newBudget = new Budget(
            GetUID(), 
            false, 
            request.target, 
            request.title, 
            scheduler, request.saveGoalIds); 
    
        await this.budgetRepository.create(newBudget);

        return { newId: newBudget.getId() };
    }
}
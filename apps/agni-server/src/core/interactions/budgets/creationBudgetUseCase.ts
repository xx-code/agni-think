import { GetUID } from "@core/adapters/libs";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { Budget } from "@core/domains/entities/budget";
import { IUsecase } from "../interfaces";
import { Scheduler } from "@core/domains/valueObjects/scheduleInfo";
import { mapperPeriod } from "@core/domains/constants";
import { MomentDateService } from "@core/domains/entities/libs";
import { CreatedDto } from "@core/dto/base";
import Repository from "@core/adapters/repository";
import { SaveGoal } from "@core/domains/entities/saveGoal";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";

export type RequestCreateBudgetSchedule = {
    period: string;
    periodTime?: number;
    dateStart: string;
    dateEnd?: string;
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
            mapperPeriod(request.schedule.period),
            MomentDateService.formatDate(request.schedule.dateStart) ,
            request.schedule.periodTime,
            request.schedule.dateEnd ? MomentDateService.formatDate(request.schedule.dateEnd) : undefined
        );

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
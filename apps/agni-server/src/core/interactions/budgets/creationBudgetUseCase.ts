import { GetUID } from "@core/adapters/libs";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { Budget } from "@core/domains/entities/budget";
import { IUsecase } from "../interfaces";
import { Scheduler } from "@core/domains/valueObjects/scheduleInfo";
import { mapperPeriod } from "@core/domains/constants";
import { MomentDateService } from "@core/domains/entities/libs";
import { CreatedDto } from "@core/dto/base";
import Repository from "@core/adapters/repository";

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
} 

export class CreationBudgetUseCase implements IUsecase<RequestCreationBudgetUseCase, CreatedDto> {
    private budgetRepository: Repository<Budget>

    constructor(
        budgetRepository: Repository<Budget>,
    ) {
        this.budgetRepository = budgetRepository
    }

    async execute(request: RequestCreationBudgetUseCase): Promise<CreatedDto> {
        if ((await this.budgetRepository.existByName(request.title)))
            throw new ResourceAlreadyExist("BUDGET_ALREADY_EXIST")

        const scheduler = new Scheduler(
            mapperPeriod(request.schedule.period),
            MomentDateService.formatDate(request.schedule.dateStart) ,
            request.schedule.periodTime,
            request.schedule.dateEnd ? MomentDateService.formatDate(request.schedule.dateEnd) : undefined
        );

        const newBudget = new Budget(GetUID(), false, request.target, request.title, scheduler); 
    
        await this.budgetRepository.create(newBudget);

        return { newId: newBudget.getId() };
    }
}
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { IUsecase } from "../interfaces";
import Repository from "@core/adapters/repository";
import { Budget } from "@core/domains/entities/budget";

export class DeleteBudgetUseCase implements IUsecase<string, void> {
    private repository: Repository<Budget>;

    constructor(budgetRepo: Repository<Budget>) {
        this.repository = budgetRepo;
    }

    async execute(id: string): Promise<void> {
        if (!(await this.repository.get(id)))
            throw new ResourceNotFoundError('BUDGET_NOT_FOUND')

        await this.repository.delete(id)
    }
};
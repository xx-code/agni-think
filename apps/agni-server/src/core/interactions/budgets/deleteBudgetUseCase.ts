import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { BudgetRepository } from "../../repositories/budgetRepository";
import { IUsecase } from "../interfaces";

export class DeleteBudgetUseCase implements IUsecase<string, void> {
    private repository: BudgetRepository;

    constructor(budgetRepo: BudgetRepository) {
        this.repository = budgetRepo;
    }

    async execute(id: string): Promise<void> {
        if (!(await this.repository.isBudgetExistById(id)))
            throw new ResourceNotFoundError('BUDGET_NOT_FOUND')

        await this.repository.delete(id)
    }
};
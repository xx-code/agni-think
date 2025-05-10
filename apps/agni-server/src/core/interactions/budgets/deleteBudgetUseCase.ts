import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { BudgetRepository } from "../../repositories/budgetRepository";

export interface IDeleteBudgetUseCase {
    execute(id: string): void;
};

export interface IDeleteBudgetUseCaseResponse {
    success(is_deleted: boolean): void;
    fail(err: Error): void;
}

export class DeleteBudgetUseCase implements IDeleteBudgetUseCase {
    private repository: BudgetRepository;
    private presenter: IDeleteBudgetUseCaseResponse;

    constructor(budgetRepo: BudgetRepository, presenter: IDeleteBudgetUseCaseResponse) {
        this.repository = budgetRepo;
        this.presenter = presenter;
    }

    async execute(id: string): Promise<void> {
        try {
            if (!(await this.repository.isBudgetExistById(id)))
                throw new ResourceNotFoundError('Budget not found')

            await this.repository.delete(id)

            this.presenter.success(true)
        } catch(err) {
            this.presenter.fail(err as Error);
        }
    }
};
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { CategoryRepository } from "../../repositories/categoryRepository";

export interface IDeleteCategoryUseCase {
    execute(id: string): void;
}

export interface IDeleteCategoryUseCaseResponse {
    success(is_deleted: boolean): void;
    fail(err: Error): void;
}

export class DeleteCategoryUseCase implements IDeleteCategoryUseCase {
    private repository: CategoryRepository;
    private presenter: IDeleteCategoryUseCaseResponse;
    
    constructor(repo: CategoryRepository, presenter: IDeleteCategoryUseCaseResponse) {
        this.repository = repo;
        this.presenter = presenter;
    }

    async execute(id: string): Promise<void> {
        try {
            if (!(await this.repository.isCategoryExistById(id)))
                throw new ResourceNotFoundError("Category not found")

            await this.repository.delete(id);
     
            this.presenter.success(true)
        } catch(err) {
            this.presenter.fail(err as Error);
        }
    }
}
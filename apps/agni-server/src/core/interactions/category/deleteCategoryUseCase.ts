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
            let category = await this.repository.get(id);

            if (category.getIsSystem())
                throw new ResourceNotFoundError("Can't delete system value")

            await this.repository.delete(id);
     
            this.presenter.success(true)
        } catch(err) {
            this.presenter.fail(err as Error);
        }
    }
}
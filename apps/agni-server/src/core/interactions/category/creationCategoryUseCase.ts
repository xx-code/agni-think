import { GetUID } from "@core/adapters/libs";
import { Category } from "@core/domains/entities/category";
import { isEmpty } from "@core/domains/helpers";
import ValidationError from "@core/errors/validationError";
import { CategoryRepository } from "../../repositories/categoryRepository";

export type RequestCreationCategoryUseCase = {
    title: string,
    icon: string,
    color: string|null|undefined
} 

export interface ICreationCategoryUseCase {
    execute(request: RequestCreationCategoryUseCase): void;
}

export interface ICreationCategoryUseCaseResponse {
    success(newCategoryId: string): void;
    fail(err: Error): void;
}

export class CreationCategoryUseCase implements ICreationCategoryUseCase {
    private repository: CategoryRepository;
    private presenter: ICreationCategoryUseCaseResponse;

    constructor(repo: CategoryRepository, presenter: ICreationCategoryUseCaseResponse) {
        this.repository = repo;
        this.presenter = presenter;
    }

    async execute(request: RequestCreationCategoryUseCase): Promise<void> {
        try {
            
            if (await this.repository.isCategoryExistByName(request.title)) {
                throw new ValidationError(`This category ${request.title} is not valid`);
            }

            let newCategory = new Category(GetUID(), request.title, request.icon, "#7f7f7f")
            if (!isEmpty(request.color))
                newCategory.setColor(request.color!)

            await this.repository.save(newCategory)

            this.presenter.success(newCategory.getId());
        } catch (err) {
            this.presenter.fail(err as Error);
        }
    }
}
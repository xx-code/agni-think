import { SAVING_CATEGORY_ID, TRANSFERT_CATEGORY_ID, FREEZE_CATEGORY_ID } from "@core/domains/constants";
import { CategoryRepository } from "../../repositories/categoryRepository";

export type CategoriesResponse = {
    categoryId: string
    title: string
    icon: string
    color: string|null 
    isSystem: boolean
}

export interface IGetAllCategoryUseCase {
    execute(): void
}

export interface IGetAllCategoryUseCaseResponse {
    success(categories: CategoriesResponse[]): void;
    fail(err: Error): void;
}

export class GetAllCategoryUseCase implements IGetAllCategoryUseCase {
    private repository: CategoryRepository;
    private presenter: IGetAllCategoryUseCaseResponse;

    constructor(repo: CategoryRepository, presenter: IGetAllCategoryUseCaseResponse) {
        this.repository = repo;
        this.presenter = presenter;
    }

    async execute(): Promise<void> {
        try {
            let results = await this.repository.getAll();

            let categories: CategoriesResponse[] = [];
            for (let result of results) {
                if ([SAVING_CATEGORY_ID, TRANSFERT_CATEGORY_ID, FREEZE_CATEGORY_ID].includes(result.getId()))
                    continue

                categories.push({
                    categoryId: result.getId(),
                    title: result.getTitle(),
                    color: result.getColor(),
                    icon: result.getIconId(),
                    isSystem: result.getIsSystem()
                });
            }
            
            this.presenter.success(categories);
        } catch(err) {
            this.presenter.fail(err as Error);
        }
    }
}
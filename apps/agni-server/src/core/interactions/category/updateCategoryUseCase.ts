import { CategoryRepository } from "../../repositories/categoryRepository";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";

export type RequestUpdateCategoryUseCase = {
    id: string
    title: string
    icon: string 
    color: string
}

export interface IUpdateCategoryUseCase {
    execute(request: RequestUpdateCategoryUseCase): void;
}

export interface IUpdateCategoryUseCaseResponse {
    success(is_updated: boolean): void;
    fail(err: Error): void;
}

export class UpdateCategoryUseCase implements IUpdateCategoryUseCase {
    private repository: CategoryRepository;
    private presenter: IUpdateCategoryUseCaseResponse;

    constructor(repo: CategoryRepository, presenter: IUpdateCategoryUseCaseResponse) {
        this.repository = repo;
        this.presenter = presenter;
    }

    async execute(request: RequestUpdateCategoryUseCase): Promise<void> {
        try {
            let fetchedCategory = await this.repository.get(request.id);
            
            if ((await this.repository.isCategoryExistByName(request.title)) && fetchedCategory.getTitle() !== request.title)
                throw new ResourceAlreadyExist(`Account name ${request.title} already exist`)
           
            fetchedCategory.setTitle(request.title)
            fetchedCategory.setIconId(request.icon)
            fetchedCategory.setColor(request.color)

            if (fetchedCategory.hasChange()) 
                await this.repository.update(fetchedCategory);

            this.presenter.success(true);
        } catch(err) {
            this.presenter.fail(err as Error);
        }
    }
}
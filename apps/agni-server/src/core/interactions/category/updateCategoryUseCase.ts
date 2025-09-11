import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import Repository from "@core/adapters/repository";
import { Category } from "@core/domains/entities/category";

export type RequestUpdateCategoryUseCase = {
    id: string
    title?: string
    icon?: string 
    color?: string
}

export class UpdateCategoryUseCase implements IUsecase<RequestUpdateCategoryUseCase, void> {
    private repository: Repository<Category>;

    constructor(repo: Repository<Category>) {
        this.repository = repo;
    }

    async execute(request: RequestUpdateCategoryUseCase): Promise<void> {
        let fetchedCategory = await this.repository.get(request.id);
        if (!fetchedCategory)
            throw new ResourceNotFoundError("CATEGORY_NOT_FOUND")
        
        if (request.title) {
            if ((await this.repository.existByName(request.title)) && fetchedCategory.getTitle() !== request.title)
                throw new ResourceAlreadyExist("CATEGORY_ALREADY_EXIST")

            fetchedCategory.setTitle(request.title)
        }

        if (request.icon)
            fetchedCategory.setIconId(request.icon)

        if (request.color)
            fetchedCategory.setColor(request.color)

        if (fetchedCategory.hasChange()) 
            await this.repository.update(fetchedCategory);
    }
}
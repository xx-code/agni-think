import { CategoryRepository } from "../../repositories/categoryRepository";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";

export type RequestUpdateCategoryUseCase = {
    id: string
    title?: string
    icon?: string 
    color?: string
}

export class UpdateCategoryUseCase implements IUsecase<RequestUpdateCategoryUseCase, void> {
    private repository: CategoryRepository;

    constructor(repo: CategoryRepository) {
        this.repository = repo;
    }

    async execute(request: RequestUpdateCategoryUseCase): Promise<void> {
        let fetchedCategory = await this.repository.get(request.id);
        if (fetchedCategory == null)
            throw new ResourceNotFoundError("CATEGORY_NOT_FOUND")
        
        if (request.title) {
            if ((await this.repository.isCategoryExistByName(request.title)) && fetchedCategory.getTitle() !== request.title)
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
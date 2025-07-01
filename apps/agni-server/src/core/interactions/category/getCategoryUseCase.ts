import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { CategoryRepository } from "../../repositories/categoryRepository";
import { IUsecase } from "../interfaces";

export type GetCategoryDto = {
    categoryId: string
    title: string
    icon: string
    color: string|null 
    isSystem: boolean
}

export class GetCategoryUseCase implements IUsecase<string, GetCategoryDto> {
    private repository: CategoryRepository;

    constructor(repo: CategoryRepository) {
        this.repository = repo;
    }

    async execute(id: string): Promise<GetCategoryDto> {
        let category = await this.repository.get(id);
        if (category == null)
            throw new ResourceNotFoundError("CATEGORY_NOT_FOUND")

        return {
            categoryId: category.getId(),
            icon: category.getIconId(),
            title: category.getTitle(),
            color: category.getColor(),
            isSystem: category.getIsSystem()
        }
    }
}
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { IUsecase } from "../interfaces";
import { Category } from "@core/domains/entities/category";
import Repository from "@core/adapters/repository";

export type GetCategoryDto = {
    categoryId: string
    title: string
    icon: string
    color: string|null 
    isSystem: boolean
}

export class GetCategoryUseCase implements IUsecase<string, GetCategoryDto> {
    private repository: Repository<Category>;

    constructor(repo: Repository<Category>) {
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
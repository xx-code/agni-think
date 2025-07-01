import { GetUID } from "@core/adapters/libs";
import { Category } from "@core/domains/entities/category";
import { isEmpty } from "@core/domains/helpers";
import ValidationError from "@core/errors/validationError";
import { CategoryRepository } from "../../repositories/categoryRepository";
import { IUsecase } from "../interfaces";
import { CreatedDto } from "@core/dto/base";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";

export type RequestCreationCategoryUseCase = {
    title: string,
    icon: string,
    color?: string
    isSystem?: boolean
} 


export class CreationCategoryUseCase implements IUsecase<RequestCreationCategoryUseCase, CreatedDto> {
    private repository: CategoryRepository;

    constructor(repo: CategoryRepository) {
        this.repository = repo;
    }

    async execute(request: RequestCreationCategoryUseCase): Promise<CreatedDto> {
        if (await this.repository.isCategoryExistByName(request.title))
            throw new ResourceAlreadyExist("CATEGORY_ALREADY_EXIST")

        let newCategory = new Category(GetUID(), request.title, request.icon, "#7f7f7f", request.isSystem?request.isSystem : false)
        if (!isEmpty(request.color))
            newCategory.setColor(request.color!)

        await this.repository.save(newCategory)

        return { newId: newCategory.getId() }
    }
}
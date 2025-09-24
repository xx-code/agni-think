import { GetUID } from "@core/adapters/libs";
import { Category } from "@core/domains/entities/category";
import { isEmpty } from "@core/domains/helpers";
import { IUsecase } from "../interfaces";
import { CreatedDto } from "@core/dto/base";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import Repository from "@core/adapters/repository";

export type RequestCreationCategoryUseCase = {
    title: string,
    icon: string,
    color?: string
    isSystem?: boolean
} 


export class CreationCategoryUseCase implements IUsecase<RequestCreationCategoryUseCase, CreatedDto> {
    private repository: Repository<Category>

    constructor(repo: Repository<Category>) {
        this.repository = repo;
    }

    async execute(request: RequestCreationCategoryUseCase): Promise<CreatedDto> {
        if (await this.repository.existByName(request.title))
            throw new ResourceAlreadyExist("CATEGORY_ALREADY_EXIST")

        let newCategory = new Category(GetUID(), request.title, request.icon, "#7f7f7f", request.isSystem?request.isSystem : false)
        if (!isEmpty(request.color))
            newCategory.setColor(request.color!)

        await this.repository.create(newCategory)

        return { newId: newCategory.getId() }
    }
}
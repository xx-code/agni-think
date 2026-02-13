import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { IUsecase } from "../interfaces";
import UnExpectedError from "@core/errors/unExpectedError";
import ResourceInService from "@core/errors/resourceInService";
import Repository from "@core/adapters/repository";
import { Category } from "@core/domains/entities/category";

export class DeleteCategoryUseCase implements IUsecase<string, void> {
    private repository: Repository<Category>;
    
    constructor(repo: Repository<Category>) {
        this.repository = repo
    }

    async execute(id: string): Promise<void> {
        let category = await this.repository.get(id);
        if (!category)
            throw new ResourceNotFoundError("CATEGORY_NOT_FOUND")

        if (category.getIsSystem())
            throw new UnExpectedError("CANT_DELETE_SYS_CATEGORY") 

        await this.repository.delete(id);
    }
}
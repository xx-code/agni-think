import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { CategoryUseChecker } from "../../repositories/categoryRepository";
import { IUsecase } from "../interfaces";
import UnExpectedError from "@core/errors/unExpectedError";
import ResourceInService from "@core/errors/resourceInService";
import Repository from "@core/adapters/repository";
import { Category } from "@core/domains/entities/category";

export class DeleteCategoryUseCase implements IUsecase<string, void> {
    private repository: Repository<Category>;
    private checker: CategoryUseChecker;
    
    constructor(repo: Repository<Category>, checker: CategoryUseChecker) {
        this.repository = repo
        this.checker = checker 
    }

    async execute(id: string): Promise<void> {
        let category = await this.repository.get(id);
        if (!category)
            throw new ResourceNotFoundError("CATEGORY_NOT_FOUND")

        if (category.getIsSystem())
            throw new UnExpectedError("CANT_DELETE_SYS_CATEGORY") 

        if (await this.checker.isInUse(id)) {
            throw new ResourceInService("CATEGORY_IN_USE")
        }

        await this.repository.delete(id);
    }
}
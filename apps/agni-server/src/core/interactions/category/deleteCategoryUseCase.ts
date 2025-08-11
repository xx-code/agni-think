import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { CategoryRepository, CategoryUseChecker } from "../../repositories/categoryRepository";
import { IUsecase } from "../interfaces";
import UnExpectedError from "@core/errors/unExpectedError";
import ResourceInService from "@core/errors/resourceInService";

export class DeleteCategoryUseCase implements IUsecase<string, void> {
    private repository: CategoryRepository;
    private checker: CategoryUseChecker;
    
    constructor(repo: CategoryRepository, checker: CategoryUseChecker) {
        this.repository = repo
        this.checker = checker 
    }

    async execute(id: string): Promise<void> {
        let category = await this.repository.get(id);
        if (category == null)
            throw new ResourceNotFoundError("CATEGORY_NOT_FOUND")

        if (category.getIsSystem())
            throw new UnExpectedError("CANT_DELETE_SYS_CATEGORY") 

        if (await this.checker.isInUse(id)) {
            throw new ResourceInService("CATEGORY_IN_USE")
        }

        await this.repository.delete(id);
    }
}
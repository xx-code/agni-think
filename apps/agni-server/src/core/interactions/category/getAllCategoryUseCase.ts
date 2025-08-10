import { SAVING_CATEGORY_ID, TRANSFERT_CATEGORY_ID, FREEZE_CATEGORY_ID } from "@core/domains/constants";
import { CategoryRepository } from "../../repositories/categoryRepository";
import { IUsecase } from "../interfaces";
import { ListDto } from "@core/dto/base";

export type GetAllCategoryDto = {
    categoryId: string
    title: string
    icon: string
    color: string|null 
    isSystem: boolean
}

export class GetAllCategoryUseCase implements IUsecase<void, ListDto<GetAllCategoryDto>> {
    private repository: CategoryRepository;

    constructor(repo: CategoryRepository) {
        this.repository = repo;
    }

    async execute(): Promise<ListDto<GetAllCategoryDto>> {
        let results = await this.repository.getAll();

        let categories: GetAllCategoryDto[] = [];
        for (let result of results) {
            categories.push({
                categoryId: result.getId(),
                title: result.getTitle(),
                color: result.getColor(),
                icon: result.getIconId(),
                isSystem: result.getIsSystem()
            });
        }
        
        return { items: categories, totals: categories.length};
    }
}
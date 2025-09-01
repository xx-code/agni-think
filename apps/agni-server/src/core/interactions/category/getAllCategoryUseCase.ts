import { CategoryRepository } from "../../repositories/categoryRepository";
import { IUsecase } from "../interfaces";
import { ListDto, QueryAllFetch } from "@core/dto/base";

export type GetAllCategoryDto = {
    categoryId: string
    title: string
    icon: string
    color: string|null 
    isSystem: boolean
}

export class GetAllCategoryUseCase implements IUsecase<QueryAllFetch, ListDto<GetAllCategoryDto>> {
    private repository: CategoryRepository;

    constructor(repo: CategoryRepository) {
        this.repository = repo;
    }

    async execute(request: QueryAllFetch): Promise<ListDto<GetAllCategoryDto>> {
        let results = await this.repository.getAll({
            limit: request.limit,
            offset: request.offset,
            queryAll: request.queryAll
        });

        let categories: GetAllCategoryDto[] = [];
        for (let result of results.items) {
            categories.push({
                categoryId: result.getId(),
                title: result.getTitle(),
                color: result.getColor(),
                icon: result.getIconId(),
                isSystem: result.getIsSystem()
            });
        }
        
        return { items: categories, totals: results.total};
    }
}
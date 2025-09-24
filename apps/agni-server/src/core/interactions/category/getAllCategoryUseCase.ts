import Repository from "@core/adapters/repository";
import { IUsecase } from "../interfaces";
import { ListDto, QueryFilter } from "@core/dto/base";
import { Category } from "@core/domains/entities/category";

export type GetAllCategoryDto = {
    categoryId: string
    title: string
    icon: string
    color: string|null 
    isSystem: boolean
}

export class GetAllCategoryUseCase implements IUsecase<QueryFilter, ListDto<GetAllCategoryDto>> {
    private repository: Repository<Category>;

    constructor(repo: Repository<Category>) {
        this.repository = repo;
    }

    async execute(request: QueryFilter): Promise<ListDto<GetAllCategoryDto>> {
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
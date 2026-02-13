import { ListDto, QueryFilter } from "@core/dto/base";
import { IUsecase } from "../interfaces";
import Repository from "@core/adapters/repository";
import { Tag } from "@core/domains/entities/tag";

export type GetAllTagDto = {
    id: string
    value: string
    color: string|null
    isSystem: boolean
}

export class GetAllTagUseCase implements IUsecase<QueryFilter, ListDto<GetAllTagDto>> {
    private repository: Repository<Tag>;

    constructor(repo: Repository<Tag>) {
        this.repository = repo;
    }

    async execute(request: QueryFilter): Promise<ListDto<GetAllTagDto>> {
        let results = await this.repository.getAll({
            limit: request.limit,
            offset: request.offset,
            queryAll: request.queryAll
        });

        let tags: GetAllTagDto[] = []
        for(let result of results.items) {
            tags.push({
                id: result.getId(),
                value: result.getValue(),
                color: result.getColor(),
                isSystem: result.getIsSystem()
            })
        }  

        return { items: tags, totals: results.total }
    }
}
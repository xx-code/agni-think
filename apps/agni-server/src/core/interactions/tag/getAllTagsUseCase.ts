import { ListDto } from "@core/dto/base";
import { TagRepository } from "../../repositories/tagRepository";
import { IUsecase } from "../interfaces";

export type GetAllTagDto = {
    id: string
    value: string
    color: string|null
    isSystem: boolean
}

export class GetAllTagUseCase implements IUsecase<void, ListDto<GetAllTagDto>> {
    private repository: TagRepository;

    constructor(repo: TagRepository) {
        this.repository = repo;
    }

    async execute(): Promise<ListDto<GetAllTagDto>> {
        let results = await this.repository.getAll();

        let tags: GetAllTagDto[] = []
        for(let result of results) {
            tags.push({
                id: result.getId(),
                value: result.getValue(),
                color: result.getColor(),
                isSystem: result.getIsSystem()
            })
        }  

        return { items: tags, totals: tags.length }
    }
}
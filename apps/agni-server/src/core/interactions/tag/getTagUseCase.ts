import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { IUsecase } from "../interfaces";
import Repository from "@core/adapters/repository";
import { Tag } from "@core/domains/entities/tag";

export type GetTagDto = {
    id: string
    value: string
    color: string|null
    isSystem: boolean
}

export class GetTagUseCase implements IUsecase<string, GetTagDto> {
    private repository: Repository<Tag>;

    constructor(repo: Repository<Tag>) {
        this.repository = repo;
    }

    async execute(id: string): Promise<GetTagDto> {
        let tag = await this.repository.get(id);
        if (tag === null)
            throw new ResourceNotFoundError("TAG_NOT_FOUND")

        return {
            id: tag.getId(),
            value: tag.getValue(),
            color: tag.getColor(),
            isSystem: tag.getIsSystem()
        }
    }
}
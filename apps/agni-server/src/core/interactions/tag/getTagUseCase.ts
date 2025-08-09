import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { TagRepository } from "../../repositories/tagRepository";
import { IUsecase } from "../interfaces";

export type GetTagDto = {
    id: string
    value: string
    color: string|null
    isSystem: boolean
}

export class GetTagUseCase implements IUsecase<string, GetTagDto> {
    private repository: TagRepository;

    constructor(repo: TagRepository) {
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
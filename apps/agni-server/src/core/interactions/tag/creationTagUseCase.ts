import { GetUID } from "@core/adapters/libs";
import { Tag } from "@core/domains/entities/tag";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { TagRepository } from "@core/repositories/tagRepository";
import { IUsecase } from "../interfaces";
import { CreatedDto } from "@core/dto/base";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";

export type RequestCreationTagUseCase = {
    value: string
    color: string
    isSystem?: boolean
} 

export class CreationTagUseCase implements IUsecase<RequestCreationTagUseCase, CreatedDto> {
    private tagRepo: TagRepository;

    constructor(repo: TagRepository) {
        this.tagRepo = repo;
    }

    async execute(request: RequestCreationTagUseCase): Promise<CreatedDto> {
        if (await this.tagRepo.isTagExistByName(request.value))
            throw new ResourceAlreadyExist("TAG_ALREADY_EXIST")

        let newTag = new Tag(GetUID(), request.value, request.color, request.isSystem)

        await this.tagRepo.save(newTag);

        return { newId: newTag.getId() }
    }
}
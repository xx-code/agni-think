import { GetUID } from "@core/adapters/libs";
import { Tag } from "@core/domains/entities/tag";
import { IUsecase } from "../interfaces";
import { CreatedDto } from "@core/dto/base";
import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import Repository from "@core/adapters/repository";

export type RequestCreationTagUseCase = {
    value: string
    color: string
    isSystem?: boolean
} 

export class CreationTagUseCase implements IUsecase<RequestCreationTagUseCase, CreatedDto> {
    private tagRepo: Repository<Tag>;

    constructor(repo: Repository<Tag>) {
        this.tagRepo = repo;
    }

    async execute(request: RequestCreationTagUseCase): Promise<CreatedDto> {
        if (await this.tagRepo.existByName(request.value))
            throw new ResourceAlreadyExist("TAG_ALREADY_EXIST")

        let newTag = new Tag(GetUID(), request.value, request.color, request.isSystem)

        await this.tagRepo.create(newTag);

        return { newId: newTag.getId() }
    }
}
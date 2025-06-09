import { GetUID } from "@core/adapters/libs";
import { Tag } from "@core/domains/entities/tag";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { TagRepository } from "@core/repositories/tagRepository";


export type RequestCreationTagUseCase = {
    value: string
    color: string
    isSystem?: boolean
} 

export interface ICreationTagUseCase {
    execute(request: RequestCreationTagUseCase): void;
}

export interface ICreationTagUseCaseResponse {
    success(newTagId: string): void;
    fail(err: Error): void;
}

export class CreationTagUseCase implements ICreationTagUseCase {
    private tagRepo: TagRepository;
    private presenter: ICreationTagUseCaseResponse;

    constructor(repo: TagRepository, presenter: ICreationTagUseCaseResponse) {
        this.tagRepo = repo;
        this.presenter = presenter;
    }

    async execute(request: RequestCreationTagUseCase): Promise<void> {
        try {
            if (await this.tagRepo.isTagExistByName(request.value))
                throw new ResourceNotFoundError("Tag already exist")

            let newTag = new Tag(GetUID(), request.value, request.color, request.isSystem)

            await this.tagRepo.save(newTag);

            this.presenter.success(newTag.getId());
        } catch (err) {
            this.presenter.fail(err as Error);
        }
    }
}
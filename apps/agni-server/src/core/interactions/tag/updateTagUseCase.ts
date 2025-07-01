import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { TagRepository } from "@core/repositories/tagRepository";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";


export type RequestUpdateTagUseCase = {
    id: string
    value: string
    color: string
} 

export class UpdateTagUseCase implements IUsecase<RequestUpdateTagUseCase, void> {
    private repository: TagRepository;

    constructor(repo: TagRepository) {
        this.repository = repo;
    }

    async execute(request: RequestUpdateTagUseCase): Promise<void> {
        let fetchedTag = await this.repository.get(request.id)
        if (fetchedTag === null)
            throw new ResourceNotFoundError("TAG_NOT_FOUND")

        if ((await this.repository.isTagExistByName(request.value)) && fetchedTag.getValue() !== request.value)
            throw new ResourceAlreadyExist("TAG_ALREADY_EXIST")

        fetchedTag.setValue(request.value)
        fetchedTag.setColor(request.color)

        if (fetchedTag.hasChange())
            await this.repository.update(fetchedTag)
    }
}
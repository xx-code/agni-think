import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import Repository from "@core/adapters/repository";
import { Tag } from "@core/domains/entities/tag";


export type RequestUpdateTagUseCase = {
    id: string
    value?: string
    color?: string
} 

export class UpdateTagUseCase implements IUsecase<RequestUpdateTagUseCase, void> {
    private repository: Repository<Tag>;

    constructor(repo: Repository<Tag>) {
        this.repository = repo;
    }

    async execute(request: RequestUpdateTagUseCase): Promise<void> {
        let fetchedTag = await this.repository.get(request.id)
        if (fetchedTag === null)
            throw new ResourceNotFoundError("TAG_NOT_FOUND")

        if (request.value) {
            if ((await this.repository.existByName(request.value)) && fetchedTag.getValue() !== request.value)
                throw new ResourceAlreadyExist("TAG_ALREADY_EXIST")

            fetchedTag.setValue(request.value)
        }

        if (request.color) {
            fetchedTag.setColor(request.color)
        }

        if (fetchedTag.hasChange())
            await this.repository.update(fetchedTag)
    }
}
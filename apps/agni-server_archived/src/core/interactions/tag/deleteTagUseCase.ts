import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { IUsecase } from "../interfaces";
import UnExpectedError from "@core/errors/unExpectedError";
import Repository from "@core/adapters/repository";
import { Tag } from "@core/domains/entities/tag";

export class DeleteTagUseCase implements IUsecase<string, void> {
    private repository: Repository<Tag>;
    
    constructor(repo: Repository<Tag>) {
        this.repository = repo;
    }

    async execute(id: string): Promise<void> {
        const tag = await this.repository.get(id)
        if (tag == null)
            throw new ResourceNotFoundError("TAG_NOT_FOUND")

        if (tag.getIsSystem())
            throw new UnExpectedError("CANT_DELETE_SYS_CATEGORY");

        await this.repository.delete(id);
    }
}
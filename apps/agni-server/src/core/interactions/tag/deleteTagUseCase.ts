import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { TagChecker } from "../../repositories/tagRepository";
import { IUsecase } from "../interfaces";
import UnExpectedError from "@core/errors/unExpectedError";
import Repository from "@core/adapters/repository";
import { Tag } from "@core/domains/entities/tag";

export class DeleteTagUseCase implements IUsecase<string, void> {
    private repository: Repository<Tag>;
    private checker: TagChecker;
    
    constructor(repo: Repository<Tag>, checker: TagChecker) {
        this.repository = repo;
        this.checker = checker;
    }

    async execute(id: string): Promise<void> {
        const tag = await this.repository.get(id)
        if (tag == null)
            throw new ResourceNotFoundError("TAG_NOT_FOUND")

        if (tag.getIsSystem())
            throw new UnExpectedError("CANT_DELETE_SYS_CATEGORY");

        if (await this.checker.inUse(id))
            throw new UnExpectedError("TAG_IN_USE")

        await this.repository.delete(id);
    }
}
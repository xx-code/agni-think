import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { TagChecker, TagRepository } from "../../repositories/tagRepository";
import { IUsecase } from "../interfaces";
import UnExpectedError from "@core/errors/unExpectedError";

export class DeleteTagUseCase implements IUsecase<string, void> {
    private repository: TagRepository;
    private checker: TagChecker;
    
    constructor(repo: TagRepository, checker: TagChecker) {
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
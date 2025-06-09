import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { TagRepository } from "../../repositories/tagRepository";

export interface IDeleteTagUseCase {
    execute(id: string): void;
}

export interface IDeleteTagUseCaseResponse {
    success(success: boolean): void;
    fail(err: Error): void;
}

export class DeleteTagUseCase implements IDeleteTagUseCase {
    private repository: TagRepository;
    private presenter: IDeleteTagUseCaseResponse;
    
    constructor(repo: TagRepository, presenter: IDeleteTagUseCaseResponse) {
        this.repository = repo;
        this.presenter = presenter;
    }

    async execute(id: string): Promise<void> {
        try {
            const tag = await this.repository.get(id)
            if (tag.getIsSystem())
                throw new ResourceNotFoundError("Can't delete system tag")

            await this.repository.delete(id);
            this.presenter.success(true);
        } catch(err) {
            this.presenter.fail(err as Error);
        }
    }
}
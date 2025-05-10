import { TagRepository } from "../../repositories/tagRepository";

export type TagOutput = {
    id: string
    value: string
    color: string|null
}

export interface IGetTagUseCase {
    execute(id: string): void;
}

export interface IGetTagUseCaseResponse {
    success(tag: TagOutput): void;
    fail(err: Error): void;
}

export class GetTagUseCase implements IGetTagUseCase {
    private repository: TagRepository;
    private presenter: IGetTagUseCaseResponse

    constructor(repo: TagRepository, presenter: IGetTagUseCaseResponse) {
        this.repository = repo;
        this.presenter = presenter;
    }

    async execute(id: string): Promise<void> {
        try {
            let tag = await this.repository.get(id);

            this.presenter.success({
                id: tag.getId(),
                value: tag.getValue(),
                color: tag.getColor()
            })
        } catch(err) {
            this.presenter.fail(err as Error)
        }
    }
}
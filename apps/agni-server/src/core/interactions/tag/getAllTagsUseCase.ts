import { TagRepository } from "../../repositories/tagRepository";

export type TagsOutput = {
    id: string
    value: string
    color: string|null
    isSystem: boolean
}
 
export interface IGetAllTagUseCase {
    execute(): void;
}

export interface IGetAllTagUseCaseResponse {
    success(tags: TagsOutput[]): void;
    fail(err: Error): void;
}

export class GetAllTagUseCase implements IGetAllTagUseCase {
    private repository: TagRepository;
    private presenter: IGetAllTagUseCaseResponse;

    constructor(repo: TagRepository, presenter: IGetAllTagUseCaseResponse) {
        this.repository = repo;
        this.presenter = presenter;
    }

    async execute(): Promise<void> {
        try {
            let results = await this.repository.getAll();

            let tags: TagsOutput[] = []
            for(let result of results) {
                tags.push({
                    id: result.getId(),
                    value: result.getValue(),
                    color: result.getColor(),
                    isSystem: result.getIsSystem()
                })
            }  

            this.presenter.success(tags);
        } catch(err) {
            this.presenter.fail(err as Error);
        }
    }
}
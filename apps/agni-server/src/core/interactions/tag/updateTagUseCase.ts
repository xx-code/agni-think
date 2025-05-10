import { ResourceAlreadyExist } from "@core/errors/resourceAlreadyExistError";
import { TagRepository } from "@core/repositories/tagRepository";


export type RequestUpdateTagUseCase = {
    id: string
    value: string
    color: string
} 

export interface IUpdateTagUseCase {
    execute(request: RequestUpdateTagUseCase): void;
}

export interface IUpdateTagUseCaseResponse {
    success(success: boolean): void;
    fail(err: Error): void;
}

export class UpdateTagUseCase implements IUpdateTagUseCase {
    private repository: TagRepository;
    private presenter: IUpdateTagUseCaseResponse;

    constructor(repo: TagRepository, presenter: IUpdateTagUseCaseResponse) {
        this.repository = repo;
        this.presenter = presenter;
    }

    async execute(request: RequestUpdateTagUseCase): Promise<void> {
        try {
            let fetchedTag = await this.repository.get(request.id)

            if ((await this.repository.isTagExistByName(request.value)) && fetchedTag.getValue() !== request.value)
                throw new ResourceAlreadyExist(`Account name ${request.value} already exist`)

            fetchedTag.setValue(request.value)
            fetchedTag.setColor(request.color)

            if (fetchedTag.hasChange())
                await this.repository.update(fetchedTag)

            this.presenter.success(true);
        } catch (err) {
            this.presenter.fail(err as Error);
        }
    }
}
import { ResourceNotFoundError } from "../../errors/resournceNotFoundError";
import { AccountRepository } from "../../repositories/accountRepository";

export interface IDeleteAccountUseCase {
    execute(id: string): void;
}

export interface IDeleteAccountUseCaseResponse {
    success(isDeleted: boolean): void;
    fail(err: Error): void;
}

export class DeleteAccountUseCase implements IDeleteAccountUseCase {
    private repository: AccountRepository;
    private presenter: IDeleteAccountUseCaseResponse;

    constructor(repo: AccountRepository, presenter: IDeleteAccountUseCaseResponse) {
        this.repository = repo;
        this.presenter = presenter;
    }

    async execute(id: string): Promise<void> {
        try {
            if (!(await this.repository.isExistById(id)))
                throw new ResourceNotFoundError('Account Not Found')

            
            await this.repository.delete(id)

            this.presenter.success(true)
        } catch(err) {
            this.presenter.fail(err as Error)
        }
    }
}
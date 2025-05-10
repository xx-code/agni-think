import { AccountRepository } from "../../repositories/accountRepository";

export interface IGetAccountUseCase {    
    execute(id: string): void;
}

export type AccountResponse = {
    accountId: string
    title: string
    balance: number
    type: string
}

export interface IGetAccountUseCaseResponse {
    success(account: AccountResponse): void
    fail(error: Error): void
}

export class GetAccountUseCase implements IGetAccountUseCase {
    private repository: AccountRepository;
    private presenter: IGetAccountUseCaseResponse;

    constructor(repo: AccountRepository, presenter: IGetAccountUseCaseResponse) {
        this.repository = repo;
        this.presenter = presenter;
    }
    
    async execute(id: string): Promise<void> {
        try {
            let account = await this.repository.get(id)

            this.presenter.success({accountId: account.getId(), title: account.getTitle(), balance: account.getBalance(), type: account.getType()});
        } catch(err) {
            this.presenter.fail(err as Error);
        }
    }
}
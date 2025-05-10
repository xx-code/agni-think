import { AccountRepository } from "../../repositories/accountRepository";
import { TransactionRepository } from "../../repositories/transactionRepository";

export interface IGetAllAccountUseCase {
    execute(): void
}

export type AccountResponse = {
    accountId: string
    title: string
    balance: number
    type: string
}

export interface IGetAllAccountUseCaseResponse {
    success(allAccount: Array<AccountResponse>): void
    fail(err: Error): void
}

export class GetAllAccountUseCase implements IGetAllAccountUseCase{
    private repository: AccountRepository;
    private presenter: IGetAllAccountUseCaseResponse;

    constructor(repo: AccountRepository, presenter: IGetAllAccountUseCaseResponse) {
        this.repository = repo;
        this.presenter = presenter;
    }

    async execute(isSaving:boolean=false): Promise<void> {
        try {
            let accounts = await this.repository.getAll();
            
            let accountsDisplay: AccountResponse[] = [];
            for (let account of accounts) {
                accountsDisplay.push({accountId: account.getId(), title: account.getTitle(), balance: account.getBalance(), type: account.getType()});
            }
            this.presenter.success(accountsDisplay);
        } catch(err) {
            this.presenter.fail(err as Error);
        } 
    }
}
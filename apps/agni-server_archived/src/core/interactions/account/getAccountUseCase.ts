import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { IUsecase } from "../interfaces";
import Repository from "@core/adapters/repository";
import { Account } from "@core/domains/entities/account";

export type GetAccountDto = {
    accountId: string
    title: string
    balance: number
    type: string
}


export class GetAccountUseCase implements IUsecase<string, GetAccountDto> {
    private repository: Repository<Account>;

    constructor(repo: Repository<Account>) {
        this.repository = repo;
    }
    
    async execute(id: string): Promise<GetAccountDto> {
        let account = await this.repository.get(id)
        if (account == null)
            throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND");

        return {accountId: account.getId(), title: account.getTitle(), balance: account.getBalance(), type: account.getType()}
    }
}
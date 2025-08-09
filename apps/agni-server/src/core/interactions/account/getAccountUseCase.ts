import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { AccountRepository } from "../../repositories/accountRepository";
import { IUsecase } from "../interfaces";

export type GetAccountDto = {
    accountId: string
    title: string
    balance: number
    type: string
}


export class GetAccountUseCase implements IUsecase<string, GetAccountDto> {
    private repository: AccountRepository;

    constructor(repo: AccountRepository) {
        this.repository = repo;
    }
    
    async execute(id: string): Promise<GetAccountDto> {
        let account = await this.repository.get(id)
        if (account == null)
            throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND");

        return {accountId: account.getId(), title: account.getTitle(), balance: account.getBalance(), type: account.getType()}
    }
}
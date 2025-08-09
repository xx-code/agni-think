import { ListDto } from "@core/dto/base";
import { AccountRepository } from "../../repositories/accountRepository";
import { IUsecase } from "../interfaces";


export type GetAllAccountDto = {
    accountId: string
    title: string
    balance: number
    type: string
}


export class GetAllAccountUseCase implements IUsecase<void, ListDto<GetAllAccountDto>>{
    private repository: AccountRepository;

    constructor(repo: AccountRepository) {
        this.repository = repo;
    }

    async execute(): Promise<ListDto<GetAllAccountDto>> {
        let accounts = await this.repository.getAll();
        
        let accountsDisplay: GetAllAccountDto[] = [];
        for (let account of accounts) {
            accountsDisplay.push({accountId: account.getId(), title: account.getTitle(), balance: account.getBalance(), type: account.getType()});
        }

        return { items: accountsDisplay, totals: accountsDisplay.length}
    }
}
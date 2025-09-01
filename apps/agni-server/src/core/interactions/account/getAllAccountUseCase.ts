import { ListDto, QueryAllFetch } from "@core/dto/base";
import { AccountRepository } from "../../repositories/accountRepository";
import { IUsecase } from "../interfaces";


export type GetAllAccountDto = {
    accountId: string
    title: string
    balance: number
    type: string
}


export class GetAllAccountUseCase implements IUsecase<QueryAllFetch, ListDto<GetAllAccountDto>>{
    private repository: AccountRepository;

    constructor(repo: AccountRepository) {
        this.repository = repo;
    }

    async execute(request: QueryAllFetch): Promise<ListDto<GetAllAccountDto>> {
        let accounts = await this.repository.getAll({
            offset: request.offset,
            limit: request.limit,
            queryAll: request.queryAll
        });
        
        let accountsDisplay: GetAllAccountDto[] = [];
        for (let account of accounts.items) {
            accountsDisplay.push({accountId: account.getId(), title: account.getTitle(), balance: account.getBalance(), type: account.getType()});
        }

        return { items: accountsDisplay, totals: accounts.total}
    }
}
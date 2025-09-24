import { ListDto, QueryFilter } from "@core/dto/base";
import { IUsecase } from "../interfaces";
import { GetAccountBalanceByPeriodDto, RequestGetAccountBalanceByPeriod } from "./getPastAccountBalanceByPeriod";
import Repository from "@core/adapters/repository";
import { Account } from "@core/domains/entities/account";


export type GetAllAccountWithPastBalanceDto = {
    accountId: string
    title: string
    balance: number
    pastBalance: number
    type: string
}

export type RequestGetAllAccountPastBalanceUseCase = QueryFilter & {
    period: string 
    periodTime: number
}


export class GetAllAccountWithPastBalanceUseCase implements IUsecase<RequestGetAllAccountPastBalanceUseCase, ListDto<GetAllAccountWithPastBalanceDto>>{
    private repository: Repository<Account>;
    private getAccountBalanceByPeriod: IUsecase<RequestGetAccountBalanceByPeriod, GetAccountBalanceByPeriodDto[]>

    constructor(
        repo: Repository<Account>, 
        getAccountBalance: IUsecase<RequestGetAccountBalanceByPeriod, GetAccountBalanceByPeriodDto[]>
    ) {
        this.repository = repo;
        this.getAccountBalanceByPeriod = getAccountBalance
    }

    async execute(request: RequestGetAllAccountPastBalanceUseCase): Promise<ListDto<GetAllAccountWithPastBalanceDto>> {
        let accounts = await this.repository.getAll({
            limit: request.limit,
            offset: request.offset,
            queryAll: request.queryAll
        });
        const pastbalances = await this.getAccountBalanceByPeriod.execute({ 
            period: request.period, 
            periodTime: request.periodTime, 
            accountIds: accounts.items.map(i => i.getId()) })
        
        let accountsDisplay: GetAllAccountWithPastBalanceDto[] = [];
        for (let accountPastBalance of pastbalances) { 
            let account = accounts.items.find(i => i.getId() === accountPastBalance.accountId)
            if (account)
                accountsDisplay.push({
                    accountId: account.getId(), 
                    title: account.getTitle(), 
                    balance: account.getBalance(), 
                    pastBalance: account.getBalance() + accountPastBalance.balance,
                    type: account.getType()
                });
        }

        return { items: accountsDisplay, totals: accountsDisplay.length}
    }
}
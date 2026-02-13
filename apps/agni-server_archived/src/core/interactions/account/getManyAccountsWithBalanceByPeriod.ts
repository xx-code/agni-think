import Repository from "@core/adapters/repository";
import { IUsecase } from "../interfaces";
import { GetAccountBalanceByPeriodDto, RequestGetAccountBalanceByPeriod } from "./getPastAccountBalanceByPeriod";
import { Account } from "@core/domains/entities/account";


export type GetManyAccountWithPastBalanceDto = {
    accountId: string
    title: string
    balance: number
    pastBalance: number
    type: string
}

export type RequestGetManyAccountPastBalance = {
    period: string 
    periodTime: number
    accountIds: string[]
}


export class GetAllAccountWithPastBalanceUseCase implements IUsecase<RequestGetManyAccountPastBalance, GetManyAccountWithPastBalanceDto[]>{
    private repository: Repository<Account>;
    private getAccountBalanceByPeriod: IUsecase<RequestGetAccountBalanceByPeriod, GetAccountBalanceByPeriodDto[]>

    constructor(
        repo: Repository<Account>, 
        getAccountBalance: IUsecase<RequestGetAccountBalanceByPeriod, GetAccountBalanceByPeriodDto[]>
    ) {
        this.repository = repo;
        this.getAccountBalanceByPeriod = getAccountBalance
    }

    async execute(request: RequestGetManyAccountPastBalance): Promise<GetManyAccountWithPastBalanceDto[]> {
        let accounts = await this.repository.getManyByIds(request.accountIds);
        const pastbalances = await this.getAccountBalanceByPeriod.execute({ 
            period: request.period, 
            periodTime: request.periodTime, 
            accountIds: accounts.map(i => i.getId()) })
        
        let accountsDisplay: GetManyAccountWithPastBalanceDto[] = [];
        for (let accountPastBalance of pastbalances) { 
            let account = accounts.find(i => i.getId() === accountPastBalance.accountId)
            if (account)
                accountsDisplay.push({
                    accountId: account.getId(), 
                    title: account.getTitle(), 
                    balance: account.getBalance(), 
                    pastBalance: account.getBalance() + accountPastBalance.balance,
                    type: account.getType()
                });
        }

        return accountsDisplay
    }
}
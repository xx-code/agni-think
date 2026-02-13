import { IUsecase } from "../interfaces";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { GetAccountBalanceByPeriodDto, RequestGetAccountBalanceByPeriod } from "../account/getPastAccountBalanceByPeriod";
import { MomentDateService } from "@core/domains/entities/libs";
import { mapperPeriod } from "@core/domains/constants";
import Repository, { PatrimonySnapshotFilter } from "@core/adapters/repository";
import { Account } from "@core/domains/entities/account";
import { Patrimony } from "@core/domains/entities/patrimony";
import { PatrimonySnapshot } from "@core/domains/entities/patrimonySnapshot";

export type GetPatrimonyAccountDto = {
    accountId: string
    title: string
    balance: number
    pastBalance: number
}

export type GetPatrimonyDto = {
    id: string
    title: string
    accounts: GetPatrimonyAccountDto[],
    amount: number,
    currentSnapshotBalance: number
    pastSnapshotBalance: number
    type: string
}

export type RequestGetPatrimony = {
    patrimonyId: string
    period: string
    periodTime: number
}
 
export class GetPatrimonyUseCase implements IUsecase<RequestGetPatrimony, GetPatrimonyDto > {
    private accountRepo: Repository<Account>
    private patrimonyRepo: Repository<Patrimony>
    private snapshotRepo: Repository<PatrimonySnapshot>
    private getAccountBalanceByPeriod: IUsecase<RequestGetAccountBalanceByPeriod, GetAccountBalanceByPeriodDto[]>

    constructor(accountRepo: Repository<Account>, 
            patrimonyRepo: Repository<Patrimony>, 
            patrimonyTransactionRepo: Repository<PatrimonySnapshot>,
            getAccountBalanceByPeriod: IUsecase<RequestGetAccountBalanceByPeriod, GetAccountBalanceByPeriodDto[]> 
        ) {
        this.accountRepo = accountRepo
        this.patrimonyRepo = patrimonyRepo
        this.snapshotRepo = patrimonyTransactionRepo
        this.getAccountBalanceByPeriod = getAccountBalanceByPeriod
    }

    async execute(request: RequestGetPatrimony): Promise<GetPatrimonyDto> {
        const patrimony = await this.patrimonyRepo.get(request.patrimonyId)
            
        if (!patrimony)
            throw new ResourceNotFoundError("PATRIMONY_NOT_FOUND")


        const period = mapperPeriod(request.period)
        const beginDate = MomentDateService.getUTCDateSubstraction(new Date(), period, request.periodTime)
        const { startDate, endDate } = MomentDateService.getUTCDateByPeriod(beginDate, period, request.periodTime)
        const accounts = await this.accountRepo.getManyByIds(patrimony.getAccounts().map(i => i.accountId))
        const accountPastBalances = await this.getAccountBalanceByPeriod.execute({ 
            period: mapperPeriod(request.period), 
            periodTime: request.periodTime, accountIds: patrimony.getAccounts().map(i => i.accountId)})

        const resAccounts: GetPatrimonyAccountDto[] = []

        accounts.forEach(account => {
            const pastBalance = accountPastBalances.find(i => i.accountId === account.getId())?.balance || 0
            resAccounts.push({
                accountId: account.getId(),
                title: account.getTitle(),
                balance: account.getBalance(),
                pastBalance: account.getBalance() + pastBalance,
            })
        })

        let accountBalance = accounts.reduce((acc:number, account) => acc + account.getBalance(), 0)
        let accountPeriodLastAmount = accountPastBalances.reduce((acc:number, account) => acc + account.balance, 0)

        const extendPatrimonySnapshotFilter = new PatrimonySnapshotFilter() 
        extendPatrimonySnapshotFilter.startDate = startDate
        extendPatrimonySnapshotFilter.endDate = new Date()
        extendPatrimonySnapshotFilter.patrimonyIds = [request.patrimonyId]
        let snapshots = await this.snapshotRepo.getAll({  
            limit: 0,
            offset: 0,
            queryAll: true,
            sort: {
                sortBy: 'date',
                asc: false
            }
        })  

        const currentSnaphot = snapshots.items.length > 0 ? snapshots.items[0].getCurrentBalanceObserver() : accountBalance
        const lastSnapshot = snapshots.items.length > 1 ? snapshots.items[1].getCurrentBalanceObserver() : accountBalance + accountPeriodLastAmount 

        const amount = patrimony.getAmount() + accountBalance 

        return {
            id: patrimony.getId(),
            currentSnapshotBalance: currentSnaphot,
            pastSnapshotBalance: lastSnapshot,
            amount: amount,
            accounts: resAccounts,
            title: patrimony.getTitle(),
            type: patrimony.getType()
        }
    }
}
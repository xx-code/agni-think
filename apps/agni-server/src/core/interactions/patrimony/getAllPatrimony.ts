import { IUsecase } from "../interfaces";
import { ListDto, QueryFilter } from "@core/dto/base";
import { GetAccountBalanceByPeriodDto, RequestGetAccountBalanceByPeriod } from "../account/getPastAccountBalanceByPeriod";
import { PatrimonyType, Period } from "@core/domains/constants";
import Repository, { PatrimonySnapshotFilter } from "@core/adapters/repository";
import { Account } from "@core/domains/entities/account";
import { Patrimony } from "@core/domains/entities/patrimony";
import { PatrimonySnapshot } from "@core/domains/entities/patrimonySnapshot";
import { SaveGoal } from "@core/domains/entities/saveGoal";

export type GetAllPatrimonyDto = {
    id: string
    title: string
    amount: number
    currentSnapshotBalance: number
    lastSnapshotBalance: number
    type: string
}


export class GetAllPatrimonyUseCase implements IUsecase<QueryFilter, ListDto<GetAllPatrimonyDto>> {
    private accountRepo: Repository<Account>
    private patrimonyRepo: Repository<Patrimony>
    private snapshotRepo: Repository<PatrimonySnapshot>
    private getAccountBalanceByPeriod: IUsecase<RequestGetAccountBalanceByPeriod, GetAccountBalanceByPeriodDto[]>
    private saveGoalRepo: Repository<SaveGoal>

    constructor(
        accountRepo: Repository<Account>, 
        patrimonyRepo: Repository<Patrimony>, 
        patrimonyTransactionRepo: Repository<PatrimonySnapshot>,
        saveGoalRepo: Repository<SaveGoal>,
        getAccountBalanceByPeriod: IUsecase<RequestGetAccountBalanceByPeriod, GetAccountBalanceByPeriodDto[]>,
    ) {
        this.accountRepo = accountRepo
        this.patrimonyRepo = patrimonyRepo
        this.snapshotRepo = patrimonyTransactionRepo
        this.saveGoalRepo = saveGoalRepo
        this.getAccountBalanceByPeriod = getAccountBalanceByPeriod
    }

    async execute(request: QueryFilter): Promise<ListDto<GetAllPatrimonyDto>> {
        const patrimonies = await this.patrimonyRepo.getAll({
            limit: request.limit,
            offset: request.offset,
            queryAll: request.queryAll
        })

        const resPatrimonies: GetAllPatrimonyDto[] = []

        const extendFilter = new PatrimonySnapshotFilter()
        extendFilter.patrimonyIds = patrimonies.items.map(i => i.getId())

        const snapshots = await this.snapshotRepo.getAll({ 
            limit: 2,
            offset: 0,
            queryAll: true,
            sort: {
                sortBy: 'date', 
                asc: false      
            }
        }, extendFilter)

        for(let i = 0; i < patrimonies.total; i++) {
            const patrimony = patrimonies.items[i]
            const accounts = await this.accountRepo.getManyByIds(patrimony.getAccounts().map(i => i.accountId))
            const pastbalanceAccounts = await this.getAccountBalanceByPeriod.execute({
                period: Period.MONTH, 
                periodTime: 1, 
                accountIds: accounts.map(i => i.getId())
            })

            let accountBalance = accounts.reduce((acc:number, account) => acc + account.getBalance(), 0)
            let accountPeriodLastAmount = pastbalanceAccounts.reduce((acc:number, account) => acc + account.balance, 0)

            const filterSnapshots = snapshots.items.filter(i => i.getPatrimonyId() === patrimony.getId())
            
            const currentSnaphot = filterSnapshots.length > 0 ? filterSnapshots[0].getCurrentBalanceObserver() : accountBalance
            const lastSnapshot = filterSnapshots.length > 1 ? filterSnapshots[1].getCurrentBalanceObserver() : (accountBalance + accountPeriodLastAmount) 

            const amount = patrimony.getAmount() + accountBalance 

            resPatrimonies.push({
                id: patrimony.getId(),
                amount: amount,
                currentSnapshotBalance: currentSnaphot,
                lastSnapshotBalance: lastSnapshot,
                title: patrimony.getTitle(),
                type: patrimony.getType()
            })
        } 

        const saveGoals = await this.saveGoalRepo.getAll({ queryAll: true, offset: 0, limit: 0})
        let saveAmount = 0 
        saveGoals.items.filter(i => i.getBalance().getAmount() > 0).forEach(i => {
            saveAmount += i.getBalance().getAmount() 
        })

        if (saveAmount > 0) 
            resPatrimonies.push({
                id: 'SAVE_GOAL',
                amount: saveAmount,
                currentSnapshotBalance: saveAmount,
                title: 'Epargnes pour But',
                lastSnapshotBalance: saveAmount,
                type: PatrimonyType.ASSET
            })

        return { items: resPatrimonies, totals: patrimonies.total } 
    }
}
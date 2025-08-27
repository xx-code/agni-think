import { AccountRepository } from "@core/repositories/accountRepository";
import { IUsecase } from "../interfaces";
import { PatrimonyRepository, PatrimonySnapshotRepository } from "@core/repositories/patrimonyRepository";
import { ListDto } from "@core/dto/base";
import { GetAccountBalanceByPeriodDto, RequestGetAccountBalanceByPeriod } from "../account/getPastAccountBalanceByPeriod";
import { MomentDateService } from "@core/domains/entities/libs";
import { mapperPeriod, PatrimonyType, Period } from "@core/domains/constants";
import { SavingRepository } from "@core/repositories/savingRepository";

export type GetAllPatrimonyDto = {
    id: string
    title: string
    amount: number
    currentSnapshotBalance: number
    lastSnapshotBalance: number
    type: string
}

export type RequestGetAllPatrimony = {
    period: string
    periodTime: number
}

export class GetAllPatrimonyUseCase implements IUsecase<RequestGetAllPatrimony, ListDto<GetAllPatrimonyDto>> {
    private accountRepo: AccountRepository
    private patrimonyRepo: PatrimonyRepository
    private snapshotRepo: PatrimonySnapshotRepository
    private getAccountBalanceByPeriod: IUsecase<RequestGetAccountBalanceByPeriod, GetAccountBalanceByPeriodDto[]>
    private saveGoalRepo: SavingRepository

    constructor(accountRepo: AccountRepository, 
        patrimonyRepo: PatrimonyRepository, 
        patrimonyTransactionRepo: PatrimonySnapshotRepository,
        saveGoalRepo: SavingRepository,
        getAccountBalanceByPeriod: IUsecase<RequestGetAccountBalanceByPeriod, GetAccountBalanceByPeriodDto[]>,
    ) {
        this.accountRepo = accountRepo
        this.patrimonyRepo = patrimonyRepo
        this.snapshotRepo = patrimonyTransactionRepo
        this.saveGoalRepo = saveGoalRepo
        this.getAccountBalanceByPeriod = getAccountBalanceByPeriod
    }

    async execute(request: RequestGetAllPatrimony): Promise<ListDto<GetAllPatrimonyDto>> {
        const patrimonies = await this.patrimonyRepo.getAll()

        const resPatrimonies: GetAllPatrimonyDto[] = []
        const period = mapperPeriod(request.period)
        const beginDate = MomentDateService.getUTCDateSubstraction(new Date(), period, request.periodTime)
        const { startDate, endDate } = MomentDateService.getUTCDateByPeriod(beginDate, period, request.periodTime)

        const snapshots = await this.snapshotRepo.getAll({ 
            patrimonyIds: patrimonies.items.map(i=> i.getId()), 
            startDate: startDate,
            endDate: new Date(),
            sort: 'desc'
        })

        for(let i = 0; i < patrimonies.total; i++) {
            const patrimony = patrimonies.items[i]
            const accounts = await this.accountRepo.getManyIds(patrimony.getAccounts().map(i => i.accountId))
            const pastbalanceAccounts = await this.getAccountBalanceByPeriod.execute({period: request.period, periodTime: request.periodTime, accountIds: accounts.map(i => i.getId())})

            let accountBalance = accounts.reduce((acc:number, account) => acc + account.getBalance(), 0)
            let accountPeriodLastAmount = pastbalanceAccounts.reduce((acc:number, account) => acc + account.balance, 0)

            const filterSnapshots = snapshots.items.filter(i => i.getPatrimonyId() === patrimony.getId())
            
            const currentSnaphot = filterSnapshots.length > 0 ? filterSnapshots[0].getCurrentBalanceObserver() : accountBalance
            const lastSnapshot = filterSnapshots.length > 1 ? filterSnapshots[1].getCurrentBalanceObserver() : (accountBalance + accountPeriodLastAmount) 

            const amount = patrimony.getAmount() + accountBalance 

            resPatrimonies.push({
                id: patrimony.getId(),
                amount: amount,
                currentSnapshotBalance: currentSnaphot ,
                lastSnapshotBalance: lastSnapshot,
                title: patrimony.getTitle(),
                type: patrimony.getType()
            })
        } 

        const saveGoals = await this.saveGoalRepo.getAll()
        let saveAmount = 0 
        saveGoals.filter(i => i.getBalance().getAmount() > 0).forEach(i => {
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
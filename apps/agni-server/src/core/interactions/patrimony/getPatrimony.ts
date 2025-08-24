import { AccountRepository } from "@core/repositories/accountRepository";
import { IUsecase } from "../interfaces";
import { PatrimonyRepository, PatrimonySnapshotRepository } from "@core/repositories/patrimonyRepository";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";

export type GetPatrimonyAccountDto = {
    accountId: string
    title: string
    balance: number
}

export type GetTrackBalancePatrimonyDto = {
    balance: number
    date: Date
}

export type GetPatrimonyDto = {
    id: string
    title: string
    accounts: GetPatrimonyAccountDto[],
    trackBalance: number
    type: string
}
 
export class GetPatrimonyUseCase implements IUsecase<string, GetPatrimonyDto > {
    private accountRepo: AccountRepository
    private patrimonyRepo: PatrimonyRepository
    private snapshotRepo: PatrimonySnapshotRepository

    constructor(accountRepo: AccountRepository, 
            patrimonyRepo: PatrimonyRepository, patrimonyTransactionRepo: PatrimonySnapshotRepository) {
        this.accountRepo = accountRepo
        this.patrimonyRepo = patrimonyRepo
        this.snapshotRepo = patrimonyTransactionRepo
    }

    async execute(patrimonyId: string): Promise<GetPatrimonyDto> {
        const patrimony = await this.patrimonyRepo.get(patrimonyId)
            
        if (!patrimony)
            throw new ResourceNotFoundError("PATRIMONY_NOT_FOUND")

        const accounts = await this.accountRepo.getManyIds(patrimony.getAccounts().map(i => i.accountId))
        const resAccounts: GetPatrimonyAccountDto[] = []

        let accountBalance = 0
        accounts.forEach(account => {
            resAccounts.push({
                accountId: account.getId(),
                title: account?.getTitle(),
                balance: account?.getBalance()
            })
            accountBalance += account.getBalance()
        })

        let lastSnapshots = await this.snapshotRepo.getLastest({ patrimonyIds: [patrimonyId]})  

        return {
            id: patrimony.getId(),
            trackBalance: lastSnapshots.length > 0 ? lastSnapshots[0].getCurrentBalanceObserver() : 0,
            accounts: resAccounts,
            title: patrimony.getTitle(),
            type: patrimony.getType()
        }
    }
}
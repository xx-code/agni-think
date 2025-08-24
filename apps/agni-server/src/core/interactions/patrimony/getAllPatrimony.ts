import { AccountRepository } from "@core/repositories/accountRepository";
import { IUsecase } from "../interfaces";
import { PatrimonyRepository, PatrimonySnapshotRepository } from "@core/repositories/patrimonyRepository";
import { ListDto } from "@core/dto/base";

export type GetPatrimonyAccountDto = {
    accountId: string
    title: string
    balance: number
}

export type GetAllPatrimonyDto = {
    id: string
    title: string
    accountBalance: number
    lastBalance: number
    type: string
}

export class GetAllPatrimonyUseCase implements IUsecase<void, ListDto<GetAllPatrimonyDto>> {
    private accountRepo: AccountRepository
    private patrimonyRepo: PatrimonyRepository
    private snapshotRepo: PatrimonySnapshotRepository

    constructor(accountRepo: AccountRepository, 
            patrimonyRepo: PatrimonyRepository, patrimonyTransactionRepo: PatrimonySnapshotRepository) {
        this.accountRepo = accountRepo
        this.patrimonyRepo = patrimonyRepo
        this.snapshotRepo = patrimonyTransactionRepo
    }

    async execute(request: void): Promise<ListDto<GetAllPatrimonyDto>> {
        const patrimonies = await this.patrimonyRepo.getAll()

        const resPatrimonies: GetAllPatrimonyDto[] = []
        for(let i = 0; i < patrimonies.total; i++) {
            const patrimony = patrimonies.items[i]
            const accounts = await this.accountRepo.getManyIds(patrimony.getAccounts().map(i => i.accountId))
            let accountBalance = 0
            accounts.forEach(i => { accountBalance += i.getBalance() })
            const snapshot = await this.snapshotRepo.getLastest({ patrimonyIds: [ patrimony.getId() ]})
            resPatrimonies.push({
                accountBalance: accountBalance,
                id: patrimony.getId(),
                lastBalance: snapshot.length > 0 ? snapshot[0].getCurrentBalanceObserver() : 0,
                title: patrimony.getTitle(),
                type: patrimony.getType()
            })
        } 

        return { items: resPatrimonies, totals: patrimonies.total } 
    }
}
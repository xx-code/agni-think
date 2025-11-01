import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";
import { IUsecase } from "../interfaces";
import Repository, { SaveGoalExtendFilter, TransactionFilter } from "@core/adapters/repository";
import { Account } from "@core/domains/entities/account";
import { Transaction } from "@core/domains/entities/transaction";
import { SaveGoal } from "@core/domains/entities/saveGoal";
import { TransactionFilterExtends } from "@infra/persistences/models/transactions";
import { Record } from "@core/domains/entities/record";

export type GetAccountDetailDto = {
    accountId: string
    title: string
    balance: number
    type: string
    lockedBalance: number
    freezedBalance: number
}


export class GetAccountDetailUseCase implements IUsecase<string, GetAccountDetailDto> {
    private repository: Repository<Account>;
    private transactionRepo: Repository<Transaction>
    private savingGoalRepo: Repository<SaveGoal>
    private recordRepo: Repository<Record>

    constructor(repo: Repository<Account>, transRepo: Repository<Transaction>, 
        savingRepo: Repository<SaveGoal>, recordRepo: Repository<Record>) {
        this.repository = repo;
        this.transactionRepo = transRepo
        this.savingGoalRepo = savingRepo
        this.recordRepo = recordRepo
    }
    
    async execute(id: string): Promise<GetAccountDetailDto> {
        let account = await this.repository.get(id)
        if (account == null)
            throw new ResourceNotFoundError("ACCOUNT_NOT_FOUND");

        const transactionExtendFilter = new TransactionFilter()
        transactionExtendFilter.isFreeze = true
        transactionExtendFilter.accounts = [account.getId()]
        const freezeTransacions = await this.transactionRepo.getAll({ offset: 0, limit: 1, queryAll: true }, transactionExtendFilter)
        const records = await this.recordRepo.getManyByIds(freezeTransacions.items.map(i => i.getRecordRef()))
        const freezeAmount = records.reduce((acc, curr) => acc += curr.getMoney().getAmount(), 0)

        const savingGoalExtendFilter = new SaveGoalExtendFilter()
        savingGoalExtendFilter.accountId = id
        const savingGoals = await this.savingGoalRepo.getAll({ offset: 0, limit: 1, queryAll: true }, savingGoalExtendFilter)
        const savingGoalAmount = savingGoals.items.reduce((acc, curr) => acc += curr.getBalance().getAmount(), 0)

        return {
            accountId: account.getId(), 
            title: account.getTitle(), 
            balance: account.getBalance(), 
            type: account.getType(),
            freezedBalance: freezeAmount,
            lockedBalance: savingGoalAmount
        }
    }
}
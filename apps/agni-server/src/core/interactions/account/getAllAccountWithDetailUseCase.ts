import { ListDto, QueryFilter } from "@core/dto/base";
import { IUsecase } from "../interfaces";
import Repository, { SaveGoalExtendFilter, TransactionFilter } from "@core/adapters/repository";
import { Account } from "@core/domains/entities/account";
import { Transaction } from "@core/domains/entities/transaction";
import { SaveGoal } from "@core/domains/entities/saveGoal";
import { Record } from "@core/domains/entities/record";


export type GetAllAccountWithDetailDto = {
    accountId: string
    title: string
    balance: number
    type: string
    lockedBalance: number
    freezedBalance: number
}


export class GetAllAccountWithDetailUseCase implements IUsecase<QueryFilter, ListDto<GetAllAccountWithDetailDto>>{
    private repository: Repository<Account>;
    private transactionRepo: Repository<Transaction>
    private savingGoalRepo: Repository<SaveGoal>
    private recordRepo: Repository<Record>

    constructor(repo: Repository<Account>, transRepo: Repository<Transaction>, 
        savingRepo: Repository<SaveGoal>, recordRepo: Repository<Record> ) {
        this.repository = repo;
        this.transactionRepo = transRepo
        this.savingGoalRepo = savingRepo
        this.recordRepo = recordRepo
    }

    async execute(request: QueryFilter): Promise<ListDto<GetAllAccountWithDetailDto>> {
        let accounts = await this.repository.getAll({
            offset: request.offset,
            limit: request.limit,
            queryAll: request.queryAll
        });
        
        let accountsDisplay: GetAllAccountWithDetailDto[] = [];
        for (let account of accounts.items) {
            const transactionExtendFilter = new TransactionFilter()
            transactionExtendFilter.isFreeze = true
            const freezeTransacions = await this.transactionRepo.getAll({ offset: 0, limit: 1, queryAll: true }, transactionExtendFilter)
            const records = await this.recordRepo.getManyByIds(freezeTransacions.items.map(i => i.getRecordRef()))
            const freezeAmount = records.reduce((acc, curr) => acc += curr.getMoney().getAmount(), 0)

            const savingGoalExtendFilter = new SaveGoalExtendFilter()
            savingGoalExtendFilter.accountId = account.getId()
            const savingGoals = await this.savingGoalRepo.getAll({ offset: 0, limit: 1, queryAll: true }, savingGoalExtendFilter)
            const savingGoalAmount = savingGoals.items.reduce((acc, curr) => acc += curr.getBalance().getAmount(), 0)

            accountsDisplay.push({
                accountId: account.getId(), 
                title: account.getTitle(), 
                balance: account.getBalance(), 
                type: account.getType(),
                freezedBalance: freezeAmount,
                lockedBalance: savingGoalAmount
            });
        }

        return { items: accountsDisplay, totals: accounts.total}
    }
}
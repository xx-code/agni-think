import { ListDto, QueryFilter } from "@core/dto/base";
import { IUsecase } from "../interfaces";
import Repository, { RecordFilter, SaveGoalExtendFilter, TransactionFilter } from "@core/adapters/repository";
import { Account } from "@core/domains/entities/account";
import { Transaction } from "@core/domains/entities/transaction";
import { SaveGoal } from "@core/domains/entities/saveGoal";
import { Record } from "@core/domains/entities/record";
import { AccountType } from "@core/domains/constants";
import { CreditCardAccountDetail } from "@core/domains/valueObjects/creditCardAccount";
import { BrockageAccountDetail } from "@core/domains/valueObjects/brockageAccount";

export type GetAllBrokingDetailDto = {
    managementType: string
    contributionType: string
}

export type GetAllCreditCardDetailDto = {
    creditCardLimit: number
    creditUtilisation: number
}

export type GetAllAccountWithDetailDto = {
    accountId: string
    title: string
    balance: number
    type: string
    lockedBalance: number
    freezedBalance: number
    detailForCreditCard?: GetAllCreditCardDetailDto 
    detailForBroking?: GetAllBrokingDetailDto
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
            transactionExtendFilter.accounts = [account.getId()]
            transactionExtendFilter.isFreeze = true
            const freezeTransacions = await this.transactionRepo.getAll({ offset: 0, limit: 1, queryAll: true }, transactionExtendFilter)
            const recordExtendFilter = new RecordFilter()
            recordExtendFilter.transactionIds = freezeTransacions.items.map(i => i.getId())
            const records = await this.recordRepo.getAll({offset: 0, limit: 0, queryAll: true}, recordExtendFilter)
            const freezeAmount = records.items.reduce((acc, curr) => acc += curr.getMoney().getAmount(), 0)

            const savingGoalExtendFilter = new SaveGoalExtendFilter()
            savingGoalExtendFilter.accountId = account.getId()
            const savingGoals = await this.savingGoalRepo.getAll({ offset: 0, limit: 1, queryAll: true }, savingGoalExtendFilter)
            const savingGoalAmount = savingGoals.items.reduce((acc, curr) => acc += curr.getBalance().getAmount(), 0)

            let detail: GetAllCreditCardDetailDto | GetAllBrokingDetailDto | undefined 
            switch(account.getType()) {
                case AccountType.CREDIT_CARD:
                    const creditLimit = (account.getDetail() as CreditCardAccountDetail).creditLimite
                    let utilization = 0 
                    if (account.getBalance() < 0) {
                        utilization = Number((Math.abs(account.getBalance() / creditLimit) * 100).toFixed(2))  
                    }
                    detail = { 
                        creditCardLimit: creditLimit,
                        creditUtilisation: utilization 
                    }
                    break
                case AccountType.BROKING:
                    detail = {
                        contributionType: (account.getDetail() as BrockageAccountDetail).contributionAccount,
                        managementType: (account.getDetail() as BrockageAccountDetail).managementType
                    }
                    break
                default:
                    break
            }
        
            accountsDisplay.push({
                accountId: account.getId(), 
                title: account.getTitle(), 
                balance: account.getBalance(), 
                type: account.getType(),
                freezedBalance: freezeAmount,
                lockedBalance: savingGoalAmount,
                detailForCreditCard: detail as (GetAllCreditCardDetailDto|undefined),
                detailForBroking: detail as (GetAllBrokingDetailDto|undefined)
            });
        }

        return { items: accountsDisplay, totals: accounts.total}
    }
}
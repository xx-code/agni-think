import { BudgetRepository } from "../../repositories/budgetRepository";
import { TransactionRepository } from "../../repositories/transactionRepository";
import { CategoryRepository } from "../../repositories/categoryRepository";
import { TagRepository } from "@core/repositories/tagRepository";
import { TransactionType } from "@core/domains/entities/record";
import { RecordRepository } from "@core/repositories/recordRepository";

export type BudgetCategoryOutput = {
   id: string
   title: string
   icon: string
   color: string|null
}

export type BudgetTagOutput = {
   id: string
   title: string
   color: string|null
}


export type BudgetOutput = {
    id: string,
   title: string,
   target: number,
   categories: BudgetCategoryOutput[],
   tags: BudgetTagOutput[]
   period: string|null
   periodTime: number
   currentBalance: number
   startDate: string
   updateDate: string
   endDate: string|null
}


export interface IGetBudgetUseCase {
   execute(id: string,): void;
}


export interface IGetBudgetUseCaseResponse {
   success(budget: BudgetOutput): void;
   fail(err: Error): void;
}

export interface IGetBudgetAdpater {
    budgetRepository: BudgetRepository
    transactionRepository: TransactionRepository
    categoryRepository: CategoryRepository
    recordRepository: RecordRepository
    tagRepository: TagRepository
}

export class GetBudgetUseCase implements IGetBudgetUseCase {
   private budgetRepository: BudgetRepository;
   private transactionRepository: TransactionRepository;
   private categoryRepository: CategoryRepository
   private tagRepository: TagRepository
   private recordRepository: RecordRepository
   private presenter: IGetBudgetUseCaseResponse;


   constructor(repo: IGetBudgetAdpater, presenter: IGetBudgetUseCaseResponse) {
       this.budgetRepository = repo.budgetRepository
       this.presenter = presenter
       this.categoryRepository = repo.categoryRepository
       this.recordRepository = repo.recordRepository
       this.transactionRepository = repo.transactionRepository
       this.tagRepository = repo.tagRepository
   }


async execute(id: string): Promise<void> {
    try {
        let budget = await this.budgetRepository.get(id);
           
        let transactions = await this.transactionRepository.getTransactions({
            categories: [],
            accounts: [],
            tags: [],
            budgets: [budget.getId()],
            types: [],
            startDate: budget.getDateStart(),
            endDate: budget.getDateEnd() ?? '',
            minPrice: null, 
            maxPrice: null
        });

        let currentBalance = 0
        let records = await this.recordRepository.getManyById(transactions.map(transaction => transaction.getRecordRef()))
        for (let record of records) {
            if (record.getType() === TransactionType.DEBIT)
                currentBalance += record.getMoney().getAmount()
        }
        
        let budgetDisplay: BudgetOutput = {
            id: budget.getId(),
            title: budget.getTitle(),
            categories: [],
            currentBalance: currentBalance,
            period: budget.getPeriod(),
            periodTime: budget.getPeriodTime(),
            target: budget.getTarget(),
            startDate: budget.getDateStart(),
            updateDate: budget.getDateUpdate(),
            endDate: budget.getDateEnd(),
            tags: []
        };
        this.presenter.success(budgetDisplay);
    } catch(err) {
        this.presenter.fail(err as Error);
    }
   }
}

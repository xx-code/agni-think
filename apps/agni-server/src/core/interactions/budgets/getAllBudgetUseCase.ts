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


export type BudgetTagsOutput = {
   id: string
   title: string
   color: string|null
}


export type BudgetsOutput = {
   id: string,
   title: string,
   target: number,
   categories: BudgetCategoryOutput[],
   tags: BudgetTagsOutput[]
   period: string|null
   periodTime: number
   currentBalance: number
   startDate: string
   updateDate: string
   endDate: string|null
}


export interface IGetAllBudgetUseCase {
   execute(): void;
}


export interface IGetAllBudgetUseCaseResponse {
   success(budgets: Array<BudgetsOutput>): void;
   fail(err: Error): void;
}

export interface IGetAllBudgetAdapter {
    budgetRepository: BudgetRepository
    categoryRepository: CategoryRepository
    tagRepository: TagRepository
    recordRepository: RecordRepository
    transactionRepository: TransactionRepository
}

export class GetAllBudgetUseCase implements IGetAllBudgetUseCase {
   private budgetRepository: BudgetRepository;
   private transactionRepository: TransactionRepository;
   private categoryRepository: CategoryRepository
   private tagRepository: TagRepository
   private recordRepository: RecordRepository
   private presenter: IGetAllBudgetUseCaseResponse;
  
   constructor(adapters: IGetAllBudgetAdapter, presenter: IGetAllBudgetUseCaseResponse) {
       this.budgetRepository = adapters.budgetRepository
       this.transactionRepository = adapters.transactionRepository
       this.recordRepository = adapters.recordRepository
       this.categoryRepository = adapters.categoryRepository
       this.tagRepository = adapters.tagRepository
       this.presenter = presenter;
   }


   async execute(): Promise<void> {
       try {
           let budgets = await this.budgetRepository.getAll();
        
           let budgetsDisplay = []
    
           for (let i = 0; i < budgets.length; i++) {
               let budget = budgets[i];

               let categories: BudgetCategoryOutput[] =  []
               for(let categoryId of budget.getCategories()) {
                   let category = await this.categoryRepository.get(categoryId)
                    categories.push({
                        id: category.getId(), 
                        title: category.getTitle(), 
                        icon: category.getIconId(), 
                        color: category.getColor()
                    })
               }


               let tags: BudgetTagsOutput[] = []
               for (let tagId of budget.getTags()) {
                   let tag = await this.tagRepository.get(tagId)
                   tags.push({id: tag.getId(), title: tag.getValue(), color: tag.getColor() })
               }

          
               let transactions = await this.transactionRepository.getTransactions({
                   categories: budget.getCategories(),
                   accounts: [],
                   tags: budget.getTags(),
                   type: TransactionType.DEBIT,
                   startDate: budget.getDateStart(),
                   endDate: budget.getDateEnd(),
                   minPrice: null, 
                   maxPrice: null
               });
               let currentBalance = 0
                let records = await this.recordRepository.getManyById(transactions.map(transaction => transaction.getRecordRef()))
                for (let record of records) {
                    if (record.getType() === TransactionType.DEBIT)
                        currentBalance += record.getMoney().getAmount()
                }
              
               let budgetDisplay: BudgetsOutput = {
                   id: budget.getId(),
                   title: budget.getTitle(),
                   categories: categories,
                   currentBalance: currentBalance,
                   period: budget.getPeriod(),
                   periodTime: budget.getPeriodTime(),
                   target: budget.getTarget(),
                   startDate: budget.getDateStart(),
                   updateDate: budget.getDateUpdate(),
                   endDate: budget.getDateEnd(),
                   tags: tags
               };

               budgetsDisplay.push(budgetDisplay);
           }
           this.presenter.success(budgetsDisplay)
       } catch(err) {
           this.presenter.fail(err as Error);
       }
   }
}

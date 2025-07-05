import { DateService } from "@core/adapters/libs";
import { AccountRepository } from "@core/repositories/accountRepository";
import { BudgetRepository } from "@core/repositories/budgetRepository";
import { CategoryRepository } from "@core/repositories/categoryRepository";
import { RecordRepository } from "@core/repositories/recordRepository";
import { TagRepository } from "@core/repositories/tagRepository";

export class TransactionDependencies {
    recordRepository: RecordRepository | undefined;
    categoryRepository: CategoryRepository | undefined;
    budgetRepository: BudgetRepository | undefined;
    tagRepository: TagRepository | undefined;
    accountRepository: AccountRepository | undefined;

    constructor(
        budgetRepository: BudgetRepository,
        recordRepository: RecordRepository,
        categoryRepository: CategoryRepository,
        tagRepository: TagRepository,
        accountRepository: AccountRepository,
    ) {
        recordRepository = recordRepository
        budgetRepository = budgetRepository
        categoryRepository = categoryRepository
        tagRepository = tagRepository
        accountRepository = accountRepository
    }
}
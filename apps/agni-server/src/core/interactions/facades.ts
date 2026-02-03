import Repository from "@core/adapters/repository";
import { Account } from "@core/domains/entities/account";
import { Budget } from "@core/domains/entities/budget";
import { Category } from "@core/domains/entities/category";
import { DeductionType } from "@core/domains/entities/decution";
import { Record } from "@core/domains/entities/record";
import { Tag } from "@core/domains/entities/tag";

export class TransactionDependencies {
    recordRepository: Repository<Record> | undefined;
    categoryRepository: Repository<Category> | undefined;
    budgetRepository: Repository<Budget> | undefined;
    tagRepository: Repository<Tag> | undefined;
    accountRepository: Repository<Account> | undefined;
    deductionRepository: Repository<DeductionType> | undefined;

    constructor(
        budgetRepository: Repository<Budget>,
        recordRepository: Repository<Record>,
        categoryRepository: Repository<Category>,
        tagRepository: Repository<Tag>,
        accountRepository: Repository<Account>,
        deductionRepository: Repository<DeductionType>
    ) {
        recordRepository = recordRepository
        budgetRepository = budgetRepository
        categoryRepository = categoryRepository
        tagRepository = tagRepository
        accountRepository = accountRepository
        deductionRepository = deductionRepository
    }
}
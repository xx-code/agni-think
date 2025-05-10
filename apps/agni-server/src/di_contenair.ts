import { AccountRepository } from '@core/repositories/accountRepository' 
import { MockAccountRepository } from './mock/repositories/mockAccountRepository';
import { CategoryRepository } from '@core/repositories/categoryRepository';
import { MockCategoryRepository } from './mock/repositories/mockCategoryRepository';
import { TagRepository } from '@core/repositories/tagRepository';
import { MockTagRepository } from './mock/repositories/mockTagRepository';
import { TransactionRepository } from '@core/repositories/transactionRepository';
import { MockTransactionRepository } from './mock/repositories/mockTransactionRepository';
import { RecordRepository } from '@core/repositories/recordRepository';
import { MockRecordRepository } from './mock/repositories/mockRecordRepository';
import { MockDateService } from './mock/services/date';
import { MockUnitOfWork } from './mock/repositories/mockUnitOfWorkRepository';
import { BudgetRepository } from '@core/repositories/budgetRepository';
import { MockBudgetRepository } from './mock/repositories/mockBudgetRepository';
import { Account } from '@core/domains/entities/account';
import { Category } from '@core/domains/entities/category';
import { Tag } from '@core/domains/entities/tag';
import { AccountType, FREEZE_CATEGORY_ID, SAVING_CATEGORY_ID, TRANSFERT_CATEGORY_ID } from '@core/domains/constants';
import { SavingRepository } from '@core/repositories/savingRepository';
import { MockSavingRepository } from './mock/repositories/mockSavingRepository';
import { PostgreSqlAccountRepository } from '@infra/data/postgreSQL/postgreSqlAccountRepository';
import { DateService } from '@core/adapters/libs';
import { UnitOfWorkRepository } from '@core/repositories/unitOfWorkRepository';
import { Knex } from 'knex';
import { PostgreSqlCategoryRepository } from '@infra/data/postgreSQL/postgreSqlCategoryRepository';
import { PostgreSqlTagRepository } from '@infra/data/postgreSQL/postgreSqlTagRepository';
import { PostgreSqlRecordRepository } from '@infra/data/postgreSQL/postgreSqlRecordRepository';
import { PostgreSqlTransactionRepository } from '@infra/data/postgreSQL/postgreSqlTransactionRepository';
import { PostgreSqlUnitOfWork } from '@infra/data/postgreSQL/postgreSqlUnitOfWork';
import { PostgresSqlBudgetRepository } from '@infra/data/postgreSQL/postgreSqlBudgetRepository';
import { PostgreSqlSavingRepository } from '@infra/data/postgreSQL/postgreSqlSavingRepository';
import { MomentDateService } from '@infra/services/date';


export class DiContenair { 
    accountRepository: AccountRepository 
    categoryRepository: CategoryRepository
    tagRepository: TagRepository
    recordRepository: RecordRepository
    budgetRepository: BudgetRepository
    savingRepository: SavingRepository
    transactionRepository: TransactionRepository
    unitOfWork: UnitOfWorkRepository
    
    // service
    dateService: DateService

    constructor(init: string = 'mock', knex: Knex) {
        this.accountRepository = new PostgreSqlAccountRepository(knex)
        this.categoryRepository = new PostgreSqlCategoryRepository(knex)
        this.tagRepository = new PostgreSqlTagRepository(knex)
        this.recordRepository = new PostgreSqlRecordRepository(knex)
        this.transactionRepository = new PostgreSqlTransactionRepository(knex)
        this.unitOfWork = new PostgreSqlUnitOfWork(knex)
        this.budgetRepository = new PostgresSqlBudgetRepository(knex)
        this.savingRepository = new PostgreSqlSavingRepository(knex)

        this.dateService = new MomentDateService()
        
    }

    configMock() {
        this.accountRepository = new MockAccountRepository()
        let defaultAccount = new Account("default", "account", AccountType.CHECKING)
        this.accountRepository.save(defaultAccount)

        this.categoryRepository = new MockCategoryRepository()
        this.categoryRepository.save(new Category("default", "cat-1", "icon", "#fff"))
        this.categoryRepository.save(new Category(SAVING_CATEGORY_ID, "Saving", "save"))
        this.categoryRepository.save(new Category(FREEZE_CATEGORY_ID, "freeze", "freeze"))
        this.categoryRepository.save(new Category(TRANSFERT_CATEGORY_ID, "transfert", "trans"))

        this.tagRepository = new MockTagRepository()
        this.tagRepository.save(new Tag("default", "tag-1", "#fff"))

        this.recordRepository = new MockRecordRepository()

        this.transactionRepository = new MockTransactionRepository()

        this.dateService = new MockDateService()

        this.unitOfWork = new MockUnitOfWork()

        this.budgetRepository = new MockBudgetRepository()

        this.savingRepository = new MockSavingRepository()
    }

    configPostgreSql(connector: Knex) {
        this.accountRepository = new PostgreSqlAccountRepository(connector)
        this.categoryRepository = new PostgreSqlCategoryRepository(connector)
        this.tagRepository = new PostgreSqlTagRepository(connector)
        this.recordRepository = new PostgreSqlRecordRepository(connector)
        this.transactionRepository = new PostgreSqlTransactionRepository(connector)
        this.unitOfWork = new PostgreSqlUnitOfWork(connector)
        this.budgetRepository = new PostgresSqlBudgetRepository(connector)
        this.savingRepository = new PostgreSqlSavingRepository(connector)

        this.dateService = new MockDateService()
    }
}

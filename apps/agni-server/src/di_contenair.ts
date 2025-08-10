import { MockAccountRepository } from './mock/repositories/mockAccountRepository';
import { MockCategoryRepository } from './mock/repositories/mockCategoryRepository';
import { MockTagRepository } from './mock/repositories/mockTagRepository';
import { MockTransactionRepository } from './mock/repositories/mockTransactionRepository';
import { MockRecordRepository } from './mock/repositories/mockRecordRepository';
import { MockUnitOfWork } from './mock/repositories/mockUnitOfWorkRepository';
import { MockBudgetRepository } from './mock/repositories/mockBudgetRepository';
import { MockSavingRepository } from './mock/repositories/mockSavingRepository';
import { PostgreSqlAccountRepository } from '@infra/data/postgreSQL/postgreSqlAccountRepository';
import { Knex } from 'knex';
import { PostgreSqlCategoryRepository } from '@infra/data/postgreSQL/postgreSqlCategoryRepository';
import { PostgreSqlTagRepository } from '@infra/data/postgreSQL/postgreSqlTagRepository';
import { PostgreSqlRecordRepository } from '@infra/data/postgreSQL/postgreSqlRecordRepository';
import { PostgreSqlTransactionRepository } from '@infra/data/postgreSQL/postgreSqlTransactionRepository';
import { PostgreSqlUnitOfWork } from '@infra/data/postgreSQL/postgreSqlUnitOfWork';
import { PostgresSqlBudgetRepository } from '@infra/data/postgreSQL/postgreSqlBudgetRepository';
import { PostgreSqlSavingRepository } from '@infra/data/postgreSQL/postgreSqlSavingRepository';
import { IUsecase } from '@core/interactions/interfaces';
import { CreationAccountUseCase, RequestCreationAccountUseCase } from '@core/interactions/account/creationAccountUseCase';
import { CreatedDto, ListDto } from '@core/dto/base';
import { RequestUpdateAccountUseCase, UpdateAccountUseCase } from '@core/interactions/account/updateAccountUseCase';
import { GetAccountDto, GetAccountUseCase } from '@core/interactions/account/getAccountUseCase';
import { GetAllAccountDto, GetAllAccountUseCase } from '@core/interactions/account/getAllAccountUseCase';
import { DeleteAccountUseCase } from '@core/interactions/account/deleteAccountUseCase';
import { CreationCategoryUseCase, RequestCreationCategoryUseCase } from '@core/interactions/category/creationCategoryUseCase';
import { RequestUpdateCategoryUseCase, UpdateCategoryUseCase } from '@core/interactions/category/updateCategoryUseCase';
import { GetCategoryDto, GetCategoryUseCase } from '@core/interactions/category/getCategoryUseCase';
import { GetAllCategoryDto, GetAllCategoryUseCase } from '@core/interactions/category/getAllCategoryUseCase';
import { DeleteCategoryUseCase } from '@core/interactions/category/deleteCategoryUseCase';
import { CreationTagUseCase, RequestCreationTagUseCase } from '@core/interactions/tag/creationTagUseCase';
import { RequestUpdateTagUseCase, UpdateTagUseCase } from '@core/interactions/tag/updateTagUseCase';
import { GetTagDto, GetTagUseCase } from '@core/interactions/tag/getTagUseCase';
import { GetAllTagDto, GetAllTagUseCase } from '@core/interactions/tag/getAllTagsUseCase';
import { DeleteTagUseCase } from '@core/interactions/tag/deleteTagUseCase';
import { AddTransactionUseCase, RequestAddTransactionUseCase } from '@core/interactions/transaction/addTransactionUseCase';
import { RequestUpdateTransactionUseCase, UpdateTransactionUseCase } from '@core/interactions/transaction/updateTransactionUseCase';
import { GetTransactionDto, GetTransactionUseCase } from '@core/interactions/transaction/getTransactionUseCase';
import { GetAllTransactionDto, GetPaginationTransaction, RequestGetPagination } from '@core/interactions/transaction/getPaginationTransactionUseCase';
import { RequestTransfertTransactionUseCase, TransfertTransactionUseCase } from '@core/interactions/transaction/transfertTransactionUseCase';
import { AddFreezeBalanceUseCase, RequestNewFreezeBalance } from '@core/interactions/freezerBalance/addFreezeBalanceUseCase';
import { TransactionDependencies } from '@core/interactions/facades';
import { DeleteTransactionUseCase } from '@core/interactions/transaction/deleteTransactionUseCase';
import { AutoDeleteFreezeBalanceUseCase } from '@core/interactions/freezerBalance/autoDeleteFreezeBalanceUseCase';
import { GetBalanceByUseCase, RequestGetBalanceBy } from '@core/interactions/transaction/getBalanceByUseCase';
import { CreationBudgetUseCase, RequestCreationBudgetUseCase } from '@core/interactions/budgets/creationBudgetUseCase';
import { RequestUpdateBudget, UpdateBudgetUseCase } from '@core/interactions/budgets/updateBudgetUseCase';
import { GetBudgetDto, GetBudgetUseCase } from '@core/interactions/budgets/getBudgetUseCase';
import { GetAllBudgetDto, GetAllBudgetUseCase } from '@core/interactions/budgets/getAllBudgetUseCase';
import { CreateScheduleTransactionUseCase, RequestCreateScheduleTransaction } from '@core/interactions/scheduleTransaction/createScheduleTransaction';
import { GetScheduleTransactionDto, GetScheduleTransactionUsecase } from '@core/interactions/scheduleTransaction/getScheduleTransaction';
import { GetAllScheduleTransactionDto, GetAllScheluleTransacationUseCase } from '@core/interactions/scheduleTransaction/getAllScheduleTransaction';
import { RequestUpdateScheduleTransaction, UpdateScheduleTransactionUseCase } from '@core/interactions/scheduleTransaction/updateScheduleTransaction';
import { DeleteBudgetUseCase } from '@core/interactions/budgets/deleteBudgetUseCase';
import { ApplyScheduleTransactionUsecase } from '@core/interactions/scheduleTransaction/applyScheduleTransaction';
import { DeleteScheduleTransactionUseCase } from '@core/interactions/scheduleTransaction/deleteScheduleTransaction';
import { AddSaveGoalUseCase, RequestAddSaveGoalUseCase } from '@core/interactions/saveGoal/addSaveGoal';
import { DeleteSaveGoalUseCase, RequestDeleteSaveGoal } from '@core/interactions/saveGoal/deleteSaveGoal';
import { IncreaseSaveGoalUseCase, RequestIncreaseSaveGoal } from '@core/interactions/saveGoal/increaseSaveGoal';
import { RequestUpdateSaveGoalUseCase, UpdateSaveGoalUseCase } from '@core/interactions/saveGoal/updateSaveGoal';
import { GetSaveGoalDto, GetSaveGoalUseCase } from '@core/interactions/saveGoal/getSaveGoal';
import { GetAllSaveGoalDto, GetAllSaveGoalUseCase } from '@core/interactions/saveGoal/getAllSaveGoal';
import { DecreaseSaveGoalUseCase, RequestDecreaseSaveGoal } from '@core/interactions/saveGoal/decreaseSaveGoal';
import { CompteTransactionUsecase, RequestCompleteTransactionUsecase } from '@core/interactions/transaction/CompleteTransactionUseCase';
import { PostgreSqlScheduleTransactionRepository } from '@infra/data/postgreSQL/postgreSqlScheduleTransactionRepository';
import { GetAllAccountWithPastBalanceDto, GetAllAccountWithPastBalanceUseCase, RequestGetAllAccountPastBalanceUseCase } from '@core/interactions/account/getAllAccountWithPatBalanceUseCase';

async function createTables(knex: Knex) {
    if (!(await knex.schema.hasTable('accounts')))
        await knex.schema.createTable("accounts", (table) => {
            table.uuid('account_id').primary()
            table.string('title')
            table.string('type')
            table.float('balance')
        });

    if (!(await knex.schema.hasTable('budgets')))
        await knex.schema.createTable('budgets', (table) => {
            table.uuid('budget_id').primary()
            table.string('title')
            table.float('target')
            table.json('scheduler')
            table.boolean('is_archived')
        });   

    if (!(await knex.schema.hasTable('categories')))
        await knex.schema.createTable("categories", (table) => {
            table.uuid('category_id').primary()
            table.string('title')
            table.string('color').notNullable()
            table.string('icon_id')
            table.boolean('is_system')
        });

    if (!(await knex.schema.hasTable('records')))
        await knex.schema.createTable('records', (table) => {
            table.uuid('record_id').primary()
            table.float('money_amount')
            table.date('date')
            table.string('type')
            table.string('description')
        });

    if (!(await knex.schema.hasTable('save_goals')))
        await knex.schema.createTable('save_goals', (table) => {
            table.uuid('save_goal_id').primary()
            table.string('title')
            table.float('target')
            table.float('balance')
            table.integer('desir_value')
            table.integer('importance')
            table.date('wish_due_date')
            table.string('description')
            
        });

    if (!(await knex.schema.hasTable('save_goal_items')))
        await knex.schema.createTable('save_goal_items', (table) => {
            table.uuid('save_goal_id').index().references('save_goal_id').inTable('save_goals').onDelete('CASCADE')
            table.json('save_goal')
        });

    if (!(await knex.schema.hasTable('schedule_transactions')))
        await knex.schema.createTable('schedule_transactions', (table) => {
            table.uuid('schedule_transaction_id').primary()
            table.uuid('account_id')
            table.uuid('category_id')
            table.float('amount')
            table.string('name')
            table.string('type')
            table.boolean('is_pause')
            table.boolean('is_pay')
            table.json('scheduler')
        });

    if (!(await knex.schema.hasTable('schedule_transaction_tags')))
        await knex.schema.createTable('schedule_transaction_tags', (table) => {
            table.uuid('schedule_transaction_id').index().references('schedule_transaction_id').inTable('schedule_transactions').onDelete('CASCADE')
            table.uuid('tag_id').index().references('tag_id').inTable('tags').onDelete('CASCADE')
        });

    if (!(await knex.schema.hasTable('tags')))
        await knex.schema.createTable('tags', (table) => {
            table.uuid('tag_id').primary();
            table.string('value').unique().notNullable();
            table.string('color').nullable();
            table.boolean('is_system');
        });

    if (!(await knex.schema.hasTable('transactions')))
        await knex.schema.createTable('transactions', (table) => {
            table.uuid('transaction_id').primary()
            table.uuid('account_id')
            table.uuid('record_id')
            table.uuid('category_id')
            table.string('status')
            table.string('type')
            table.date('date')
            table.boolean('is_freeze')
        });

    if (!(await knex.schema.hasTable('transaction_tags')))
        await knex.schema.createTable('transaction_tags', (table) => {
            table.uuid('transaction_id').index().references('transaction_id').inTable('transactions').onDelete('CASCADE')
            table.uuid('tag_id').index().references('tag_id').inTable('tags').onDelete('CASCADE')
        });

    if (!(await knex.schema.hasTable('transaction_budgets')))
        await knex.schema.createTable('transaction_budgets', (table) => {
            table.uuid('transaction_id').index().references('transaction_id').inTable('transactions').onDelete('CASCADE')
            table.uuid('budget_id').index().references('budget_id').inTable('budgets').onDelete('CASCADE')
        });
}

export class DiContenair {
    private services: Map<any, any>;  
    private repositories: Map<any, any>;
    private checkers: Map<any, any>;

    public accountUseCase?: {
        createAccount: IUsecase<RequestCreationAccountUseCase, CreatedDto>,
        updateAccount: IUsecase<RequestUpdateAccountUseCase, void>,
        getAccount: IUsecase<string, GetAccountDto>,
        getAllAccount: IUsecase<void, ListDto<GetAllAccountDto>>,
        getAllAccountWithBalance: IUsecase<RequestGetAllAccountPastBalanceUseCase, ListDto<GetAllAccountWithPastBalanceDto>>
        deleteAccount: IUsecase<string, void>,
    };

    public categoryUseCase?: {
        createCategory: IUsecase<RequestCreationCategoryUseCase, CreatedDto>,
        updateCategory: IUsecase<RequestUpdateCategoryUseCase, void>,
        getCategory: IUsecase<string, GetCategoryDto>,
        getAllCategory: IUsecase<void, ListDto<GetAllCategoryDto>>,
        deleteCategory: IUsecase<string, void>,
    };

    public tagUseCase?: {
        createTag: IUsecase<RequestCreationTagUseCase, CreatedDto>,
        updateTag: IUsecase<RequestUpdateTagUseCase, void>,
        getTag: IUsecase<string, GetTagDto>,
        getAllTag: IUsecase<void, ListDto<GetAllTagDto>>,
        deleteTag: IUsecase<string, void>
    }

    public transactionUseCase?: {
        createTransaction: IUsecase<RequestAddTransactionUseCase, CreatedDto>,
        updateTransaction: IUsecase<RequestUpdateTransactionUseCase, void>,
        completeTransaction: IUsecase<RequestCompleteTransactionUsecase, void>,
        getTransaction: IUsecase<string, GetTransactionDto>,
        getPaginition: IUsecase<RequestGetPagination, ListDto<GetAllTransactionDto>>,
        getBalanceBy: IUsecase<RequestGetBalanceBy, number>,
        deleteTransaction: IUsecase<string, void>,
        transfertTransaction: IUsecase<RequestTransfertTransactionUseCase, void>,
        freezeTransaction: IUsecase<RequestNewFreezeBalance, CreatedDto>
        autoFreezeTransaction: IUsecase<void, void>
    }

    public budgetUseCase?: {
        createBudget: IUsecase<RequestCreationBudgetUseCase, CreatedDto>,
        updateBudget: IUsecase<RequestUpdateBudget, void>,
        getBudget: IUsecase<string, GetBudgetDto>,
        getAllBudgets: IUsecase<void, ListDto<GetAllBudgetDto>>,
        deleteBudget: IUsecase<string, void>,
    }

    public scheduleTransactionUseCase?: {
        applyScheduleTransaction: IUsecase<void, void>,
        createScheduleTransaction: IUsecase<RequestCreateScheduleTransaction, CreatedDto>,
        deleteScheduleTransaction: IUsecase<string, void>,
        getScheduleTransaction: IUsecase<string, GetScheduleTransactionDto>,
        getAllScheduleTransaction: IUsecase<void, ListDto<GetAllScheduleTransactionDto>>,
        updateScheduleTransaction: IUsecase<RequestUpdateScheduleTransaction, void>
    }

    public saveGoalUseCase?: {
        addSaveGoal: IUsecase<RequestAddSaveGoalUseCase, CreatedDto>,
        decreaseSaveGoal: IUsecase<RequestDecreaseSaveGoal, void>,
        increaseSaveGoal: IUsecase<RequestIncreaseSaveGoal, void>,
        deleteSaveGoal: IUsecase<RequestDeleteSaveGoal, void>,
        updateSaveGoal: IUsecase<RequestUpdateSaveGoalUseCase, void>,
        getSaveGoal: IUsecase<string, GetSaveGoalDto>,
        getAllSaveGoal: IUsecase<void, ListDto<GetAllSaveGoalDto>>
    }

    constructor() {
        this.services = new Map();
        this.repositories = new Map();
        this.checkers = new Map();
    }

    registerService(name: string, service: any) {
        this.services.set(name, service)
    }

    registerRepository(name: string, service: any) {
        this.repositories.set(name, service)
    }


    async configMock() {
        this.registerRepository('account', new MockAccountRepository())
        this.registerRepository('category', new MockCategoryRepository())
        this.registerRepository('tag', new MockTagRepository())
        this.registerRepository('record', new MockRecordRepository())
        this.registerRepository('transaction', new MockTransactionRepository())
        this.registerRepository('budget', new MockBudgetRepository())
        this.registerRepository('saving', new MockSavingRepository())
        this.registerRepository('unit_of_work', new MockUnitOfWork())
    }

   async config(connector: Knex) {
        await createTables(connector);
        let accountRepository = new PostgreSqlAccountRepository(connector)
        this.registerRepository('account', accountRepository)

        let categoryRepository = new PostgreSqlCategoryRepository(connector)
        this.registerRepository('category', categoryRepository)

        let tagRepository = new PostgreSqlTagRepository(connector)
        this.registerRepository('tag', tagRepository)

        let recordRepository = new PostgreSqlRecordRepository(connector)
        this.registerRepository('record', recordRepository)

        let unitOfWork = new PostgreSqlUnitOfWork(connector)
        this.registerRepository('unit_of_work', unitOfWork)

        let budgetRepository = new PostgresSqlBudgetRepository(connector)
        this.registerRepository('budget', budgetRepository)

        let savingRepository = new PostgreSqlSavingRepository(connector)
        this.registerRepository('saving', savingRepository)

        let transactionRepository = new PostgreSqlTransactionRepository(connector)
        this.registerRepository('transaction', transactionRepository)

        let scheduleTransactionRepository = new PostgreSqlScheduleTransactionRepository(connector);
        this.registerRepository('schedule_transaction', scheduleTransactionRepository)

        // usecases
        this.registerAccountUsecases();
        this.registerCategoryUsecases();
        this.registerTagUsecases();
        this.registerTransactionUsecases();
        this.registerBudgetUsecases();
        this.registerScheduleTransactionUsecases();
        this.registerSaveGoalUsecases();
    }

    getService(name: string): any {
        return this.services.get(name)
    }

    getRepository(name: string): any {
        return this.repositories.get(name)
    }

    getChecker(name: string): any {
        return this.checkers.get(name)
    }

    private registerAccountUsecases() {
        this.accountUseCase = {
            createAccount: new CreationAccountUseCase(this.getRepository('account')),
            updateAccount: new UpdateAccountUseCase(this.getRepository('account')),
            getAccount: new GetAccountUseCase(this.getRepository('account')),
            getAllAccount: new GetAllAccountUseCase(this.getRepository('account')),
            getAllAccountWithBalance: new GetAllAccountWithPastBalanceUseCase(this.getRepository('account'), 
            this.getRepository('transaction'), this.getRepository('record')),
            deleteAccount: new DeleteAccountUseCase(this.getRepository('account'))
        } 
    }

    private registerCategoryUsecases() {
        this.categoryUseCase = {
            createCategory: new CreationCategoryUseCase(this.getRepository('category')),
            updateCategory: new UpdateCategoryUseCase(this.getRepository('category')),
            getCategory: new GetCategoryUseCase(this.getRepository('category')),
            getAllCategory: new GetAllCategoryUseCase(this.getRepository('category')),
            deleteCategory: new DeleteCategoryUseCase(this.getRepository('category'), this.getChecker('category')) 
        }
    }

    private registerTagUsecases() {
        this.tagUseCase = {
            createTag: new CreationTagUseCase(this.getRepository('tag')),
            updateTag: new UpdateTagUseCase(this.getRepository('tag')),
            getTag: new GetTagUseCase(this.getRepository('tag')),
            deleteTag: new DeleteTagUseCase(this.getRepository('tag'), this.getChecker('tag')),
            getAllTag: new GetAllTagUseCase(this.getRepository('tag'))
        }
    }
    
    private registerTransactionUsecases() {
        const transDept: TransactionDependencies = {
            accountRepository: this.getRepository('account'),
            budgetRepository: this.getRepository('budget'),
            categoryRepository: this.getRepository('category'),
            recordRepository: this.getRepository('record'),
            tagRepository: this.getRepository('tag')
        }
        const addUseCase = new AddTransactionUseCase(this.getRepository('unit_of_work'), this.getRepository('transaction'), transDept);
        const deleteUseCase = new DeleteTransactionUseCase(this.getRepository('transaction'), this.getRepository('record'), this.getRepository('unit_of_work'), this.getRepository('account'));
        this.transactionUseCase = {
            createTransaction: addUseCase,
            completeTransaction: new CompteTransactionUsecase(this.getRepository('transaction'), this.getRepository('account'), this.getRepository('record'), this.getRepository('unit_of_work')),
            updateTransaction: new UpdateTransactionUseCase(this.getRepository('transaction'), transDept, addUseCase, deleteUseCase, this.getRepository('unit_of_work')),
            transfertTransaction: new TransfertTransactionUseCase(this.getRepository('transaction'), this.getRepository('account'), this.getRepository('record'), this.getRepository('unit_of_work')),
            autoFreezeTransaction: new AutoDeleteFreezeBalanceUseCase(this.getRepository('account'), this.getRepository('transaction'), this.getRepository('record'), this.getRepository('unit_of_work')),
            deleteTransaction: deleteUseCase,
            getBalanceBy: new GetBalanceByUseCase(this.getRepository('transaction'), this.getRepository('record')),
            freezeTransaction: new AddFreezeBalanceUseCase(this.getRepository('transaction'), this.getRepository('account'), this.getRepository('record'), this.getRepository('unit_of_work')),
            getPaginition: new GetPaginationTransaction(this.getRepository('transaction'), transDept, this.getRepository('record')),
            getTransaction: new GetTransactionUseCase(this.getRepository('transaction'), this.getRepository('record'))
        }
    }

    private registerBudgetUsecases() {
        this.budgetUseCase = {
            createBudget: new CreationBudgetUseCase(this.getRepository('budget')),
            deleteBudget: new DeleteBudgetUseCase(this.getRepository('budget')),
            getBudget: new GetBudgetUseCase(this.getRepository('budget'), this.getRepository('transaction'), this.getRepository('record')),
            getAllBudgets: new GetAllBudgetUseCase(this.getRepository('budget'), this.getRepository('transaction'), this.getRepository('record')),
            updateBudget: new UpdateBudgetUseCase(this.getRepository('budget'))
        }
    }

    private registerScheduleTransactionUsecases() {
        const transDept: TransactionDependencies = {
            accountRepository: this.getRepository('account'),
            budgetRepository: this.getRepository('budget'),
            categoryRepository: this.getRepository('category'),
            recordRepository: this.getRepository('record'),
            tagRepository: this.getRepository('tag')
        }
        this.scheduleTransactionUseCase = {
            applyScheduleTransaction: new ApplyScheduleTransactionUsecase(this.getRepository('schedule_transaction'), this.getRepository('transaction'), this.getRepository('record'), this.getRepository('unit_of_work')),
            createScheduleTransaction: new CreateScheduleTransactionUseCase(transDept, this.getRepository('schedule_transaction')),
            updateScheduleTransaction: new UpdateScheduleTransactionUseCase(this.getRepository('schedule_transaction'), transDept),
            deleteScheduleTransaction: new DeleteScheduleTransactionUseCase(this.getRepository('schedule_transaction')),
            getAllScheduleTransaction: new GetAllScheluleTransacationUseCase(this.getRepository('schedule_transaction')),
            getScheduleTransaction: new GetScheduleTransactionUsecase(this.getRepository('schedule_transaction'))
        }
    }

    private registerSaveGoalUsecases() {
        this.saveGoalUseCase = {
            addSaveGoal: new AddSaveGoalUseCase(this.getRepository('saving')),
            increaseSaveGoal: new IncreaseSaveGoalUseCase(this.getRepository('cagetory'), this.getRepository('account'), 
            this.getRepository('saving'), this.getRepository('transaction'), this.getRepository('record'), this.getRepository('unit_of_work')),
            decreaseSaveGoal: new DecreaseSaveGoalUseCase(this.getRepository('category'), this.getRepository('account'), this.getRepository('saving'),
            this.getRepository('transaction'), this.getRepository('record'), this.getRepository('unit_of_work')),
            deleteSaveGoal: new DeleteSaveGoalUseCase(this.getRepository('transaction'), this.getRepository('account'), this.getRepository('saving'), 
            this.getRepository('record'), this.getRepository('unit_of_work')),
            getAllSaveGoal: new GetAllSaveGoalUseCase(this.getRepository('saving')),
            getSaveGoal: new GetSaveGoalUseCase(this.getRepository('saving')),
            updateSaveGoal: new UpdateSaveGoalUseCase(this.getRepository('saving'))
        }
    }
}

export default new DiContenair()
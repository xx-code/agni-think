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
import { MomentDateService } from '@core/domains/entities/libs';


export class DiContenair {
    private services: Map<any, any>;  
    private repositories: Map<any, any>;
    private checkers: Map<any, any>;

    public accountUseCase?: {
        createAccount: IUsecase<RequestCreationAccountUseCase, CreatedDto>,
        updateAccount: IUsecase<RequestUpdateAccountUseCase, void>,
        getAccount: IUsecase<string, GetAccountDto>,
        getAllAccount: IUsecase<void, ListDto<GetAllAccountDto>>,
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
        getTransaction: IUsecase<string, GetTransactionDto>,
        getPaginition: IUsecase<RequestGetPagination, ListDto<GetAllTransactionDto>>,
        getBalanceBy: IUsecase<RequestGetBalanceBy, number>,
        deleteTransaction: IUsecase<string, void>,
        transfertTransaction: IUsecase<RequestTransfertTransactionUseCase, void>,
        freezeTransaction: IUsecase<RequestNewFreezeBalance, CreatedDto>
        autoFreezeTransaction: IUsecase<void, void>
    }

    constructor() {
        this.services = new Map()
        this.repositories = new Map()
        this.checkers = new Map()
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
        let accountRepository = new PostgreSqlAccountRepository(connector)
        await accountRepository.initialisation()
        this.registerRepository('account', accountRepository)

        let categoryRepository = new PostgreSqlCategoryRepository(connector)
        await categoryRepository.initialisation()
        this.registerRepository('category', categoryRepository)

        let tagRepository = new PostgreSqlTagRepository(connector)
        await tagRepository.initialisation()
        this.registerRepository('tag', tagRepository)

        let recordRepository = new PostgreSqlRecordRepository(connector)
        await recordRepository.initialisation()
        this.registerRepository('record', recordRepository)

        let unitOfWork = new PostgreSqlUnitOfWork(connector)
        this.registerRepository('unit_of_work', unitOfWork)

        let budgetRepository = new PostgresSqlBudgetRepository(connector)
        await budgetRepository.initialisation()
        this.registerRepository('budget', budgetRepository)

        let savingRepository = new PostgreSqlSavingRepository(connector)
        await savingRepository.initialisation()
        this.registerRepository('saving', savingRepository)

        let transactionRepository = new PostgreSqlTransactionRepository(connector)
        await transactionRepository.initialisation()
        this.registerRepository('transaction', transactionRepository)

        // usecases
        this.registerAccountUsecases();
        this.registerCategoryUsecases();
        this.registerTagUsecases();
        this.registerTransactionUsecases();
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
}

export default new DiContenair()
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


export class DiContenair {
    private services: Map<any, any>;  
    private repositories: Map<any, any>;

    public accountUseCase?: {
        createAccount: IUsecase<RequestCreationAccountUseCase, CreatedDto>,
        updateAccount: IUsecase<RequestUpdateAccountUseCase, void>,
        getAccount: IUsecase<string, GetAccountDto>,
        getAllAccount: IUsecase<void, ListDto<GetAllAccountDto>>,
        deleteAccount: IUsecase<string, void>,
    };

    constructor() {
        this.services = new Map()
        this.repositories = new Map()
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
    }

    getService(name: string): any {
        return this.services.get(name)
    }

    getRepository(name: string): any {
        return this.repositories.get(name)
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
    
}

export default new DiContenair()
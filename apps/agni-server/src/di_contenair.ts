import { Knex } from 'knex';
import { CreationAccountUseCase, RequestCreationAccountUseCase } from '@core/interactions/account/creationAccountUseCase';
import { CreatedDto, ListDto, QueryFilter } from '@core/dto/base';
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
import { GetAllAccountWithPastBalanceDto, GetAllAccountWithPastBalanceUseCase, RequestGetAllAccountPastBalanceUseCase } from '@core/interactions/account/getAllAccountWithPatBalanceUseCase';
import { EstimationLeftAmountUseCase, GetEstimationLeftAmoutDto, RequestEstimationLeftAmount } from '@core/interactions/analystic/estimationleftAmount';
import { IAgent } from '@core/agents/interface';
import AgentGoalScoring from '@infra/agents/agentGoalScoring';
import HttpAgentPlanningAdvisor from '@infra/agents/httpAgentPlanningAdvisor';
import { GetAllSuggestPlanningDto, RequestSuggestPlanningSaveGoal, SuggestPlanningSaveGoalUseCase } from '@core/interactions/analystic/suggestPlanningSaveGoalUseCase';
import IAgentScoringGoal from '@core/agents/agentGoalScoring';
import IAgentPlanningAdvisor from '@core/agents/agentPlanningAdvisor';
import { CreatePatrimonyUseCase, RequestCreatePatrimony } from '@core/interactions/patrimony/createPatrimony';
import { RequestUpdatePatrimony, UpdatePatrimonyUseCase } from '@core/interactions/patrimony/updatePatrimony';
import { GetPatrimonyDto, GetPatrimonyUseCase, RequestGetPatrimony } from '@core/interactions/patrimony/getPatrimony';
import { GetAllPatrimonyDto, GetAllPatrimonyUseCase, RequestGetAllPatrimony } from '@core/interactions/patrimony/getAllPatrimony';
import { AddSnapshotPatrimonyUseCase, RequestAddSnapshotPatrimony } from '@core/interactions/patrimony/addSnapshotToPatrimony';
import { RequestUpdateSnapshotPatrimony, UpdateSnapshotPatrimonyUseCase } from '@core/interactions/patrimony/updateSnapshotPatrimony';
import DeletePatrimonyUseCase from '@core/interactions/patrimony/deletePatrimony';
import { RemoveSnapshotFromPatrimonyUseCase } from '@core/interactions/patrimony/removeSnapshotFromPatrimony';
import { GetAllSnapshotOfPatrimony, GetAllSnapshotPatrimonyDto, RequestAllSnapshotPatrimony } from '@core/interactions/patrimony/getAllSnapshotOfPatrimony';
import { GetAccountBalanceByPeriodDto, GetPastAccountBalanceByPeriodUseCase, RequestGetAccountBalanceByPeriod } from '@core/interactions/account/getPastAccountBalanceByPeriod';
import { CashFlowAnalyseUseCase, CashFlowResponse, RequestCashFlow } from '@core/interactions/analystic/cashflowAnalyse';
import { AnalyseBudgetResponse, AnalyseBudgetRuleUseCase, RequestAnalyseBudgetRule } from '@core/interactions/analystic/analysebudgetRule';
import Entity, { IEntity } from '@core/domains/entities/entity';
import KnexRepository from '@infra/persistences/knexRepository';
import Repository from '@core/adapters/repository';
import UnExpectedError from '@core/errors/unExpectedError';
import { Account } from '@core/domains/entities/account';
import { AccountModel, AccountModelMapper, KnexAccountTable } from '@infra/persistences/models/account';
import { CategoryModel, CategoryModelMapper, KnexCategoryTable } from '@infra/persistences/models/category';
import { Category } from '@core/domains/entities/category';
import { KnexTagTable, TagModel, TagModelMapper } from '@infra/persistences/models/tag';
import { Tag } from '@core/domains/entities/tag';
import { KnexRecordTable, RecordModel, RecordModelMapper } from '@infra/persistences/models/record';
import { Record } from '@core/domains/entities/record';
import { BudgetModel, BudgetModelMapper, KnexBudgetTable } from '@infra/persistences/models/budget';
import { Budget } from '@core/domains/entities/budget';
import { KnexScheduleTransactionTable, ScheduleTransactionMapper, ScheduleTransactionModel } from '@infra/persistences/models/scheduleTransaction';
import { ScheduleTransaction } from '@core/domains/entities/scheduleTransaction';
import { KnexTransactionTable, TransactionFilterExtends, TransactionModel, TransactionModelMapper } from '@infra/persistences/models/transactions';
import { Transaction } from '@core/domains/entities/transaction';
import { KnexSaveGoalTable, SaveGoalModel, SaveGoalModelMapper } from '@infra/persistences/models/saveGoal';
import { SaveGoal } from '@core/domains/entities/saveGoal';
import { KnexPatrimonyTable, PatrimonyModel, PatrimonyModelMapper } from '@infra/persistences/models/patrimony';
import { Patrimony } from '@core/domains/entities/patrimony';
import { KnexPatrimonySnapshotTable, PatrimonySnapshotFilterExtendAdpater, PatrimonySnapshotModel, PatrimonySnapshotModelMapper } from '@infra/persistences/models/patrimonySnapshot';
import { PatrimonySnapshot } from '@core/domains/entities/patrimonySnapshot';
import { KnexNotificationTable, NotificaitonExtendFilterAdapter, NotificationModel, NotificationModelMapper } from '@infra/persistences/models/notification';
import { IUsecase } from '@core/interactions/interfaces';
import Notification from '@core/domains/entities/notification';
import { UnitOfWorkRepository } from '@core/repositories/unitOfWorkRepository';
import { PostgreSqlUnitOfWork } from '@infra/data/postgreSQL/postgreSqlUnitOfWork';
import { IEventListener, IEventRegister } from '@core/adapters/event';
import { EventRegister } from '@infra/event';
import { PushNotificationUseCase, RequestPushNotification } from '@core/interactions/notification/pushNotification';
import { DeleteNotifcationUseCase } from '@core/interactions/notification/deleteNotification';
import { ToggleReadNotificationUseCase } from '@core/interactions/notification/toggleReadNotification';
import { GetAllNotificationDto, GetAllNotificationUseCases, NotificationQueryFilter } from '@core/interactions/notification/getAllNotication';
import { IncomeAnalysticResponse, IncomeAnalysticUseCase, RequestIncomAnalystic } from '@core/interactions/analystic/income';
import { RequestSavingAnalystic, SavingAnalysticResponse, SavingAnalysticUseCase } from '@core/interactions/analystic/savings';
import { RequestSpendAnalystic, SpendAnalysticResponse, SpendAnalysticUseCase } from '@core/interactions/analystic/spends';


export class DiContenair {
    private services: Map<any, unknown>;  
    private repositories: Map<string, Repository<any>>;
    private unitOfWork: UnitOfWorkRepository|undefined = undefined;
    private checkers: Map<any, unknown>;
    private agents: Map<string, IAgent<unknown, unknown>> 
    private eventRegister: IEventRegister|undefined = undefined

    public accountUseCase?: {
        createAccount: IUsecase<RequestCreationAccountUseCase, CreatedDto>,
        updateAccount: IUsecase<RequestUpdateAccountUseCase, void>,
        getAccount: IUsecase<string, GetAccountDto>,
        getAllAccount: IUsecase<QueryFilter, ListDto<GetAllAccountDto>>,
        getAllAccountWithBalance: IUsecase<RequestGetAllAccountPastBalanceUseCase, ListDto<GetAllAccountWithPastBalanceDto>>
        deleteAccount: IUsecase<string, void>,
        getPastAccountBalanceByPeriod: IUsecase<RequestGetAccountBalanceByPeriod, GetAccountBalanceByPeriodDto[]>
    };

    public categoryUseCase?: {
        createCategory: IUsecase<RequestCreationCategoryUseCase, CreatedDto>,
        updateCategory: IUsecase<RequestUpdateCategoryUseCase, void>,
        getCategory: IUsecase<string, GetCategoryDto>,
        getAllCategory: IUsecase<QueryFilter, ListDto<GetAllCategoryDto>>,
        deleteCategory: IUsecase<string, void>,
    };

    public tagUseCase?: {
        createTag: IUsecase<RequestCreationTagUseCase, CreatedDto>,
        updateTag: IUsecase<RequestUpdateTagUseCase, void>,
        getTag: IUsecase<string, GetTagDto>,
        getAllTag: IUsecase<QueryFilter, ListDto<GetAllTagDto>>,
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
        getAllBudgets: IUsecase<QueryFilter, ListDto<GetAllBudgetDto>>,
        deleteBudget: IUsecase<string, void>,
    }

    public scheduleTransactionUseCase?: {
        applyScheduleTransaction: IUsecase<void, void>,
        createScheduleTransaction: IUsecase<RequestCreateScheduleTransaction, CreatedDto>,
        deleteScheduleTransaction: IUsecase<string, void>,
        getScheduleTransaction: IUsecase<string, GetScheduleTransactionDto>,
        getAllScheduleTransaction: IUsecase<QueryFilter, ListDto<GetAllScheduleTransactionDto>>,
        updateScheduleTransaction: IUsecase<RequestUpdateScheduleTransaction, void>
    }

    public saveGoalUseCase?: {
        addSaveGoal: IUsecase<RequestAddSaveGoalUseCase, CreatedDto>,
        decreaseSaveGoal: IUsecase<RequestDecreaseSaveGoal, void>,
        increaseSaveGoal: IUsecase<RequestIncreaseSaveGoal, void>,
        deleteSaveGoal: IUsecase<RequestDeleteSaveGoal, void>,
        updateSaveGoal: IUsecase<RequestUpdateSaveGoalUseCase, void>,
        getSaveGoal: IUsecase<string, GetSaveGoalDto>,
        getAllSaveGoal: IUsecase<QueryFilter, ListDto<GetAllSaveGoalDto>>
    }

    public analyticUseCase?: {
        estimateLeftAmount: IUsecase<RequestEstimationLeftAmount, GetEstimationLeftAmoutDto>
        planningSaveGoalAdvisor: IUsecase<RequestSuggestPlanningSaveGoal, GetAllSuggestPlanningDto>
        cashflowAnalyse: IUsecase<RequestCashFlow, CashFlowResponse>
        analyseBudgetRule: IUsecase<RequestAnalyseBudgetRule, AnalyseBudgetResponse>
        incomeAnalystic: IUsecase<RequestIncomAnalystic, IncomeAnalysticResponse>
        savingAnalystic: IUsecase<RequestSavingAnalystic, SavingAnalysticResponse>
        spendAnalystic: IUsecase<RequestSpendAnalystic, SpendAnalysticResponse>
    }

    public patrimonyUseCase?: {
        createPatrimony: IUsecase<RequestCreatePatrimony, CreatedDto>,
        deletePatrimony: IUsecase<string, void>
        updatePatrimony: IUsecase<RequestUpdatePatrimony, void>
        getPatrimony: IUsecase<RequestGetPatrimony, GetPatrimonyDto>
        getAllPatrimony: IUsecase<RequestGetAllPatrimony, ListDto<GetAllPatrimonyDto>>
        addSnapshotPatrimony: IUsecase<RequestAddSnapshotPatrimony, CreatedDto>
        removeSnapshotPatrimony: IUsecase<string, void>
        updateSnapshotPatrimony: IUsecase<RequestUpdateSnapshotPatrimony, void>
        getSnapshotPatrimony: IUsecase<RequestAllSnapshotPatrimony, ListDto<GetAllSnapshotPatrimonyDto>>
    }

    public notificationUseCase?: {
        pushNotification: IUsecase<RequestPushNotification, CreatedDto> & IEventListener
        deleteNotification: IUsecase<string, void>
        toggleReadNotification: IUsecase<string, void>
        getAllNotification: IUsecase<NotificationQueryFilter, ListDto<GetAllNotificationDto>>
    }

    constructor() {
        this.services = new Map();
        this.repositories = new Map();
        this.checkers = new Map();
        this.agents = new Map(); 
    }

    registerAgent<In, Out>(name: string, agent: IAgent<In, Out>) {
        this.agents.set(name, agent) 
    }

    getAgent(name: string): IAgent<unknown, unknown>|undefined {
        return this.agents.get(name)
    }

    registerService(name: string, service: any) {
        this.services.set(name, service)
    }

    getService(name: string): any {
        return this.services.get(name)
    } 

    registerRepository<TEntity extends Entity>(name: string, repo: Repository<TEntity>) {
        this.repositories.set(name, repo)
    }

    getRepository<TEntity extends Entity>(name: string): Repository<TEntity> {
        const repo = this.repositories.get(name)
        if (repo)
            return repo  

        throw new UnExpectedError(`REPOSITORY_NOT_SETUP_${name}`)
    } 

    getChecker(name: string): any {
        return this.checkers.get(name)
    } 


    async configMock() {
    }

    private async registerKnexRepositories(connector: Knex) {
        const accountTable = new KnexAccountTable()
        await accountTable.createTable(connector)
        const accountModelMapper = new AccountModelMapper()
        const accountRepository = new KnexRepository<Account, AccountModel>(
            connector, accountTable, accountModelMapper
        ) 
        this.registerRepository('account', accountRepository)

        const categoryTable = new KnexCategoryTable()
        await categoryTable.createTable(connector)
        const categoryModelMapper = new CategoryModelMapper()
        const categoryRepository = new KnexRepository<Category, CategoryModel>(
            connector, categoryTable, categoryModelMapper
        )
        this.registerRepository('category', categoryRepository)

        const tagTable = new KnexTagTable()
        await tagTable.createTable(connector)
        const tagModelMapper = new TagModelMapper()
        const tagRepository = new KnexRepository<Tag, TagModel>(
            connector, tagTable, tagModelMapper
        )
        this.registerRepository('tag', tagRepository)

        const recordTable = new KnexRecordTable() 
        await recordTable.createTable(connector)
        const recordModelMapper = new RecordModelMapper()
        const recordRepository = new KnexRepository<Record, RecordModel>(
            connector, recordTable, recordModelMapper
        )
        this.registerRepository('record', recordRepository)

        const budgetTable = new KnexBudgetTable() 
        await budgetTable.createTable(connector)
        const budgetModelMapper = new BudgetModelMapper()
        const budgetRepository = new KnexRepository<Budget, BudgetModel>(
            connector, budgetTable, budgetModelMapper 
        )
        this.registerRepository('budget', budgetRepository)

        const scheduleTransactionTable = new KnexScheduleTransactionTable() 
        await scheduleTransactionTable.createTable(connector)
        const scheduleTransactionMapper = new ScheduleTransactionMapper()
        const scheduleTransactionRepository = new KnexRepository<ScheduleTransaction, ScheduleTransactionModel>(
            connector, scheduleTransactionTable, scheduleTransactionMapper
        )
        this.registerRepository('schedule_transaction', scheduleTransactionRepository)

        const transactionTable = new KnexTransactionTable()
        await transactionTable.createTable(connector)
        const transactionModelMapper = new TransactionModelMapper()
        const transactionFitlerAdapter = new TransactionFilterExtends()
        const transactionRepository = new KnexRepository<Transaction, TransactionModel>(
            connector, transactionTable, transactionModelMapper, transactionFitlerAdapter
        )
        this.registerRepository('transaction', transactionRepository)

        const saveGoalTable = new KnexSaveGoalTable()
        await saveGoalTable.createTable(connector)
        const saveGoalModelMapper = new SaveGoalModelMapper()
        const saveGoalRepository = new KnexRepository<SaveGoal, SaveGoalModel>(
            connector, saveGoalTable, saveGoalModelMapper
        )
        this.registerRepository('save_goal', saveGoalRepository)

        const patrimonyTable = new KnexPatrimonyTable()
        await patrimonyTable.createTable(connector)
        const patrimonyModelMapper = new PatrimonyModelMapper()
        const patrimonyRepository = new KnexRepository<Patrimony, PatrimonyModel>(
            connector, patrimonyTable, patrimonyModelMapper
        )
        this.registerRepository('patrimony', patrimonyRepository)

        const patrimonySnapshotTable = new KnexPatrimonySnapshotTable()
        await patrimonySnapshotTable.createTable(connector)
        const patrimonySnapshotModelMapper = new PatrimonySnapshotModelMapper()
        const patrimonySnapshotFilterAdapter = new PatrimonySnapshotFilterExtendAdpater()
        const patrimonySnapshotRepository = new KnexRepository<PatrimonySnapshot, PatrimonySnapshotModel>(
            connector, patrimonySnapshotTable, patrimonySnapshotModelMapper, patrimonySnapshotFilterAdapter 
        )
        this.registerRepository('patrimony_snapshot', patrimonySnapshotRepository)

        const notificationTable = new KnexNotificationTable()
        await notificationTable.createTable(connector)
        const notificationModelMapper = new NotificationModelMapper()
        const notificationFilterAdpater = new NotificaitonExtendFilterAdapter()
        const notificationRepository = new KnexRepository<Notification, NotificationModel>(
            connector, notificationTable, notificationModelMapper , notificationFilterAdpater
        )
        this.registerRepository('notification', notificationRepository)

        this.unitOfWork = new PostgreSqlUnitOfWork(connector)
    }

    async config(connector: Knex) {
        // adapter 
        this.eventRegister = new EventRegister()
        

        // repository
        await this.registerKnexRepositories(connector)

        // agents
        const agentGoalRanking = new AgentGoalScoring();
        this.registerAgent('goal_ranking', agentGoalRanking);
        const agentPlanningAdvisor = new HttpAgentPlanningAdvisor();
        this.registerAgent('planning', agentPlanningAdvisor);

        // usecases
        this.registerNotificationUseCases(this.eventRegister)
        this.registerAccountUsecases();
        this.registerCategoryUsecases();
        this.registerTagUsecases();
        this.registerTransactionUsecases();
        this.registerBudgetUsecases();
        this.registerScheduleTransactionUsecases();
        this.registerSaveGoalUsecases();
        this.registerAnalyticUseCases();
        this.registerPatrimonyUseCases(); 
    }  

    private registerAccountUsecases() {
        const getAccountBalanceByPeriodUc = new GetPastAccountBalanceByPeriodUseCase(
            this.getRepository('transaction'), 
            this.getRepository('record'))
        this.accountUseCase = {
            createAccount: new CreationAccountUseCase(this.getRepository('account')),
            updateAccount: new UpdateAccountUseCase(this.getRepository('account')),
            getAccount: new GetAccountUseCase(this.getRepository('account')),
            getAllAccount: new GetAllAccountUseCase(this.getRepository('account')),
            getAllAccountWithBalance: new GetAllAccountWithPastBalanceUseCase(
                this.getRepository('account'), 
                getAccountBalanceByPeriodUc),
            deleteAccount: new DeleteAccountUseCase(this.getRepository('account')),
            getPastAccountBalanceByPeriod: getAccountBalanceByPeriodUc
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
        if (!this.unitOfWork)
            throw new UnExpectedError("UNIT_OF_WORK_NOT_SETUP")

        if (!this.eventRegister)
            throw new UnExpectedError("EVENT_REGISTER_NOT_SETUP")

        const transDept: TransactionDependencies = {
            accountRepository: this.getRepository('account'),
            budgetRepository: this.getRepository('budget'),
            categoryRepository: this.getRepository('category'),
            recordRepository: this.getRepository('record'),
            tagRepository: this.getRepository('tag')
        }
        const addUseCase = new AddTransactionUseCase(
            this.unitOfWork, 
            this.getRepository('transaction'), transDept);
        const deleteUseCase = new DeleteTransactionUseCase(
            this.getRepository('transaction'), 
            this.getRepository('record'), 
            this.unitOfWork, 
            this.getRepository('account'));
        this.transactionUseCase = {
            createTransaction: addUseCase,
            completeTransaction: new CompteTransactionUsecase(this.getRepository('transaction'), this.getRepository('account'), this.getRepository('record'), this.unitOfWork),
            updateTransaction: new UpdateTransactionUseCase(this.getRepository('transaction'), transDept, addUseCase, deleteUseCase, this.unitOfWork),
            transfertTransaction: new TransfertTransactionUseCase(this.getRepository('transaction'), this.getRepository('account'), this.getRepository('record'), this.unitOfWork),
            autoFreezeTransaction: new AutoDeleteFreezeBalanceUseCase(
                this.getRepository('account'), 
                this.getRepository('transaction'), 
                this.getRepository('record'), 
                this.unitOfWork, this.eventRegister),
            deleteTransaction: deleteUseCase,
            getBalanceBy: new GetBalanceByUseCase(this.getRepository('transaction'), this.getRepository('record')),
            freezeTransaction: new AddFreezeBalanceUseCase(this.getRepository('transaction'), this.getRepository('account'), this.getRepository('record'), this.unitOfWork),
            getPaginition: new GetPaginationTransaction(this.getRepository('transaction'), transDept),
            getTransaction: new GetTransactionUseCase(this.getRepository('transaction'), this.getRepository('record'))
        }
    }

    private registerBudgetUsecases() {
        this.budgetUseCase = {
            createBudget: new CreationBudgetUseCase(this.getRepository('budget'), this.getRepository('save_goal')),
            deleteBudget: new DeleteBudgetUseCase(this.getRepository('budget')),
            getBudget: new GetBudgetUseCase(this.getRepository('budget'), 
            this.getRepository('transaction'), this.getRepository('record'), this.getRepository('save_goal')),
            getAllBudgets: new GetAllBudgetUseCase(this.getRepository('budget'), 
            this.getRepository('transaction'), this.getRepository('record'), this.getRepository('save_goal')),
            updateBudget: new UpdateBudgetUseCase(this.getRepository('budget'), this.getRepository('save_goal'))
        }
    }

    private registerScheduleTransactionUsecases() {
        if (!this.unitOfWork)
            throw new UnExpectedError("UNIT_OF_WORK_NOT_SETUP")

        if (!this.eventRegister)
            throw new UnExpectedError("EVENT_REGISTER_NOT_SETUP")

        const transDept: TransactionDependencies = {
            accountRepository: this.getRepository('account'),
            budgetRepository: this.getRepository('budget'),
            categoryRepository: this.getRepository('category'),
            recordRepository: this.getRepository('record'),
            tagRepository: this.getRepository('tag')
        }
        this.scheduleTransactionUseCase = {
            applyScheduleTransaction: new ApplyScheduleTransactionUsecase(
                this.getRepository('schedule_transaction'), 
                this.getRepository('transaction'), 
                this.getRepository('record'), this.unitOfWork, this.eventRegister),
            createScheduleTransaction: new CreateScheduleTransactionUseCase(transDept, this.getRepository('schedule_transaction')),
            updateScheduleTransaction: new UpdateScheduleTransactionUseCase(this.getRepository('schedule_transaction'), transDept),
            deleteScheduleTransaction: new DeleteScheduleTransactionUseCase(this.getRepository('schedule_transaction')),
            getAllScheduleTransaction: new GetAllScheluleTransacationUseCase(this.getRepository('schedule_transaction')),
            getScheduleTransaction: new GetScheduleTransactionUsecase(this.getRepository('schedule_transaction'))
        }
    }

    private registerSaveGoalUsecases() {
        if (!this.unitOfWork)
            throw new UnExpectedError("UNIT_OF_WORK_NOT_SETUP")

        this.saveGoalUseCase = {
            addSaveGoal: new AddSaveGoalUseCase(this.getRepository('save_goal')),
            increaseSaveGoal: new IncreaseSaveGoalUseCase(this.getRepository('account'), this.getRepository('save_goal'), this.getRepository('transaction'), 
            this.getRepository('record'), this.unitOfWork),

            decreaseSaveGoal: new DecreaseSaveGoalUseCase(this.getRepository('account'), this.getRepository('save_goal'),
            this.getRepository('transaction'), this.getRepository('record'), this.unitOfWork),

            deleteSaveGoal: new DeleteSaveGoalUseCase(this.getRepository('transaction'), this.getRepository('account'), this.getRepository('save_goal'), 
            this.getRepository('record'), this.unitOfWork),

            getAllSaveGoal: new GetAllSaveGoalUseCase(this.getRepository('save_goal')),
            getSaveGoal: new GetSaveGoalUseCase(this.getRepository('save_goal')),
            updateSaveGoal: new UpdateSaveGoalUseCase(this.getRepository('save_goal'))
        }
    }

    private registerAnalyticUseCases() {
        const estimateUseCase = new EstimationLeftAmountUseCase(this.getRepository('budget'), this.getRepository('transaction'),
            this.getRepository('account'), this.getRepository('record'), this.getRepository('schedule_transaction'))
        this.analyticUseCase = {
            estimateLeftAmount: estimateUseCase,
            planningSaveGoalAdvisor: new SuggestPlanningSaveGoalUseCase(estimateUseCase, this.getRepository('account'), this.getRepository('save_goal'), 
            this.getAgent('goal_ranking')! as IAgentScoringGoal, this.getAgent('planning')! as IAgentPlanningAdvisor),
            cashflowAnalyse: new CashFlowAnalyseUseCase(this.getRepository('transaction'), this.getRepository('record')),
            analyseBudgetRule: new AnalyseBudgetRuleUseCase(this.getRepository('transaction'), this.getRepository('record'), this.getRepository('account')),
            incomeAnalystic: new IncomeAnalysticUseCase(this.getRepository('record'), this.getRepository('transaction')),
            savingAnalystic: new SavingAnalysticUseCase(this.getRepository('transaction'), this.getRepository('account'), this.getRepository('record')),
            spendAnalystic: new SpendAnalysticUseCase(this.getRepository('record'), this.getRepository('transaction'), this.getRepository('category'), this.getRepository('tag')) 
        }
    }

    private registerPatrimonyUseCases() {
        if (!this.unitOfWork)
            throw new UnExpectedError("UNIT_OF_WORK_NOT_SETUP")

        this.patrimonyUseCase = {
            addSnapshotPatrimony: new AddSnapshotPatrimonyUseCase(
                this.getRepository('patrimony'),
                this.getRepository('patrimony_snapshot')
            ),
            createPatrimony: new CreatePatrimonyUseCase(
                this.getRepository('patrimony'),
                this.getRepository('account'),
                this.getRepository('patrimony_snapshot'),
                this.unitOfWork
            ),
            deletePatrimony: new DeletePatrimonyUseCase(
                this.getRepository('patrimony'),
                this.unitOfWork
            ),
            getAllPatrimony: new GetAllPatrimonyUseCase(
                this.getRepository('account'),
                this.getRepository('patrimony'),
                this.getRepository('patrimony_snapshot'),
                this.getRepository('save_goal'),
                this.accountUseCase?.getPastAccountBalanceByPeriod!
            ),
            getPatrimony: new GetPatrimonyUseCase(
                this.getRepository('account'),
                this.getRepository('patrimony'),
                this.getRepository('patrimony_snapshot'),
                this.accountUseCase?.getPastAccountBalanceByPeriod!
            ),
            removeSnapshotPatrimony: new RemoveSnapshotFromPatrimonyUseCase(
                this.getRepository('patrimony_snapshot')
            ),
            updatePatrimony: new UpdatePatrimonyUseCase(
                this.getRepository('patrimony'),
                this.getRepository('account')
            ),
            updateSnapshotPatrimony: new UpdateSnapshotPatrimonyUseCase(
                this.getRepository('patrimony_snapshot')
            ),
            getSnapshotPatrimony: new GetAllSnapshotOfPatrimony(
                this.getRepository('patrimony_snapshot')
            )
        }
    }
    
    private registerNotificationUseCases(event: IEventRegister) {
        this.notificationUseCase = {
            pushNotification: new PushNotificationUseCase(this.getRepository('notification')),
            deleteNotification: new DeleteNotifcationUseCase(this.getRepository('notification')),
            toggleReadNotification: new ToggleReadNotificationUseCase(this.getRepository('notification')),
            getAllNotification: new GetAllNotificationUseCases(this.getRepository('notification'))
        }
        event.subscribe('notification', this.notificationUseCase.pushNotification!)
    }
}

export default new DiContenair()
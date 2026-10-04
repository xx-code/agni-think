package persistences

import domain.entities.Account
import domain.entities.AgentSuggestion
import domain.entities.BankRegister
import domain.entities.Budget
import domain.entities.Category
import domain.entities.Currency
import domain.entities.Deduction
import domain.entities.ExternalTransaction
import domain.entities.FinancePrinciple
import domain.entities.FinanceReport
import domain.entities.Goal
import domain.entities.IncomeSource
import domain.entities.InternalLoan
import domain.entities.Invoice
import domain.entities.Notification
import domain.entities.Patrimony
import domain.entities.PatrimonySnapshot
import domain.entities.Profile
import domain.entities.Provision
import domain.entities.Fund
import domain.entities.ScheduleInvoice
import domain.entities.SpendingPeriod
import domain.entities.SpendingPeriodTemplate
import domain.entities.Tag
import domain.entities.Transaction
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import persistences.jbdc_model.JbdcAccountModel
import persistences.jbdc_model.JdbcAccountSnapshotBalance
import persistences.jbdc_model.JdbcAgentSuggestionModel
import persistences.jbdc_model.JdbcBankRegisterModel
import persistences.jbdc_model.JdbcBudgetModel
import persistences.jbdc_model.JdbcCategoryModel
import persistences.jbdc_model.JdbcCurrencyModel
import persistences.jbdc_model.JdbcDeductionModel
import persistences.jbdc_model.JdbcExternalTransactionModel
import persistences.jbdc_model.JdbcFinancePrincipleModel
import persistences.jbdc_model.JdbcFinanceReportModel
import persistences.jbdc_model.JdbcFundModel
import persistences.jbdc_model.JdbcGoalModel
import persistences.jbdc_model.JdbcIncomeSourceModel
import persistences.jbdc_model.JdbcInternalLoanModal
import persistences.jbdc_model.JdbcInvoiceModel
import persistences.jbdc_model.JdbcNotificationModel
import persistences.jbdc_model.JdbcPatrimonyModel
import persistences.jbdc_model.JdbcPatrimonySnapshotModel
import persistences.jbdc_model.JdbcProfileModel
import persistences.jbdc_model.JdbcProvisionModel
import persistences.jbdc_model.JdbcScheduleInvoiceModel
import persistences.jbdc_model.JdbcSpendingPeriodModel
import persistences.jbdc_model.JdbcSpendingPeriodTemplateModel
import persistences.jbdc_model.JdbcTagModel
import persistences.jbdc_model.JdbcTransactionModel
import java.util.UUID

// Account
@Repository
interface AccountStorage: GenericStorage<JbdcAccountModel, UUID>

@Component
class AccountRepository(
    storage: AccountStorage,
    accountModelMapper: IMapper<JbdcAccountModel, Account>,
    queryAdapter: JdbcQueryAdapter
): JdbcRepository<JbdcAccountModel, Account>(storage = storage, accountModelMapper, queryAdapter = queryAdapter)

// Category
@Repository
interface CategoryStorage: GenericStorage<JdbcCategoryModel, UUID>

@Component
class CategoryRepository(
    storage: CategoryStorage,
    categoryModelMapper: IMapper<JdbcCategoryModel, Category>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcCategoryModel, Category>(storage = storage, categoryModelMapper, queryAdapter)

// Currency
@Repository
interface CurrencyStorage: GenericStorage<JdbcCurrencyModel, UUID>

@Component
class CurrencyRepository(
    storage: CurrencyStorage,
    currencyModelMapper: IMapper<JdbcCurrencyModel, Currency>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcCurrencyModel, Currency>(storage = storage, currencyModelMapper, queryAdapter)

// Deduction
@Repository
interface DeductionStorage: GenericStorage<JdbcDeductionModel, UUID>

@Component
class DeductionRepository(
    storage: DeductionStorage,
    deductionModelMapper: IMapper<JdbcDeductionModel, Deduction>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcDeductionModel, Deduction>(storage = storage, deductionModelMapper, queryAdapter,)

// Invoice
@Repository
interface InvoiceStorage: GenericStorage<JdbcInvoiceModel, UUID>

@Component
class InvoiceRepository(
    storage: InvoiceStorage,
    invoiceModelMapper: IMapper<JdbcInvoiceModel, Invoice>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcInvoiceModel, Invoice>(storage = storage, invoiceModelMapper, queryAdapter)

// Notification
@Repository
interface NotificationStorage: GenericStorage<JdbcNotificationModel, UUID>

@Component
class NotificationRepository(
    storage: NotificationStorage,
    notificationModelMapper: IMapper<JdbcNotificationModel, Notification>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcNotificationModel, Notification>(storage = storage, notificationModelMapper, queryAdapter)

// Patrimony
@Repository
interface PatrimonyStorage: GenericStorage<JdbcPatrimonyModel, UUID>

@Component
class PatrimonyRepository(
    storage: PatrimonyStorage,
    patrimonyModelMapper: IMapper<JdbcPatrimonyModel, Patrimony>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcPatrimonyModel, Patrimony>(storage = storage, patrimonyModelMapper, queryAdapter)

// PatrimonySnapshot
@Repository
interface PatrimonySnapshotStorage: GenericStorage<JdbcPatrimonySnapshotModel, UUID>

@Component
class PatrimonySnapshotRepository(
    storage: PatrimonySnapshotStorage,
    patrimonySnapshotMapper: IMapper<JdbcPatrimonySnapshotModel, PatrimonySnapshot>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcPatrimonySnapshotModel, PatrimonySnapshot>(storage, patrimonySnapshotMapper, queryAdapter)

// Proisionable
@Repository
interface ProvisionableStorage: GenericStorage<JdbcProvisionModel, UUID>

@Component
class ProvisionableRepository(
    storage: ProvisionableStorage,
    provisionModlModelMapper: IMapper<JdbcProvisionModel, Provision>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcProvisionModel, Provision>(storage = storage, provisionModlModelMapper, queryAdapter)

//Saving Goal
@Repository
interface SavingGoalStorage: GenericStorage<JdbcFundModel, UUID>

@Component
class SavingGoalRepository(
    storage: SavingGoalStorage,
    storageModelMapper: IMapper<JdbcFundModel, Fund>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcFundModel, Fund>(storage = storage, modelMapper = storageModelMapper, queryAdapter)

// ScheduleInvoice
@Repository
interface ScheduleInvoiceStorage: GenericStorage<JdbcScheduleInvoiceModel, UUID>

@Component
class ScheduleInvoiceRepository(
    storage: ScheduleInvoiceStorage,
    scheduleModelMapper: IMapper<JdbcScheduleInvoiceModel, ScheduleInvoice>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcScheduleInvoiceModel, ScheduleInvoice>( storage = storage, scheduleModelMapper, queryAdapter)

// Tag
@Repository
interface TagStorage: GenericStorage<JdbcTagModel, UUID>

@Component
class TagRepository(
    storage: TagStorage,
    tagModelMapper: IMapper<JdbcTagModel, Tag>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcTagModel, Tag>(storage = storage, tagModelMapper, queryAdapter)

// Transaction
@Repository
interface TransactionStorage: GenericStorage<JdbcTransactionModel, UUID>

@Component
class TransactionRepository(
    storage: TransactionStorage,
    transactionModelMapper: IMapper<JdbcTransactionModel, Transaction>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcTransactionModel, Transaction>( storage = storage, transactionModelMapper, queryAdapter)

// Budget
@Repository
interface BudgetStorage: GenericStorage<JdbcBudgetModel, UUID>

@Component
class BudgetRepository(
    storage: BudgetStorage,
    budgetModelMapper: IMapper<JdbcBudgetModel, Budget>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcBudgetModel, Budget>(storage, budgetModelMapper, queryAdapter)

@Repository
interface FinancePrincipleStorage: GenericStorage<JdbcFinancePrincipleModel, UUID>

@Component
class FinancePrincipleRepository(
    storage: FinancePrincipleStorage,
    financePrincipleMapper: IMapper<JdbcFinancePrincipleModel, FinancePrinciple>,
    queryAdapter: JdbcQueryAdapter,
) : JdbcRepository<JdbcFinancePrincipleModel, FinancePrinciple>(storage, financePrincipleMapper, queryAdapter)

@Repository
interface IncomeSourceStorage: GenericStorage<JdbcIncomeSourceModel, UUID>

@Component
class IncomeSourceRepository(
    storage: IncomeSourceStorage,
    incomeSourceMapper: IMapper<JdbcIncomeSourceModel, IncomeSource>,
    queryAdapter: JdbcQueryAdapter,
) : JdbcRepository<JdbcIncomeSourceModel, IncomeSource>(storage, incomeSourceMapper, queryAdapter)

@Repository
interface AgentSuggestionStorage: GenericStorage<JdbcAgentSuggestionModel, UUID>

@Component
class AgentSuggestionRepository(
    storage: AgentSuggestionStorage,
    agentSuggestionMapper: IMapper<JdbcAgentSuggestionModel, AgentSuggestion>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcAgentSuggestionModel, AgentSuggestion>(storage, agentSuggestionMapper, queryAdapter)

@Repository
interface BankRegisterStorage: GenericStorage<JdbcBankRegisterModel, UUID>

@Component
class BankRegisterRepository(
    storage: BankRegisterStorage,
    bankRegisterMapper: IMapper<JdbcBankRegisterModel, BankRegister>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcBankRegisterModel, BankRegister>(storage, bankRegisterMapper, queryAdapter)

@Repository
interface ExternalTransactionStorage: GenericStorage<JdbcExternalTransactionModel, UUID>

@Component
class ExternalBankRegisterRepository(
    storage: ExternalTransactionStorage,
    externalTransactionModelMapper: IMapper<JdbcExternalTransactionModel, ExternalTransaction>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcExternalTransactionModel, ExternalTransaction>(storage, externalTransactionModelMapper, queryAdapter)

@Repository
interface FinanceReportStorage: GenericStorage<JdbcFinanceReportModel, UUID>

@Component
class FinanceReportRepository(
    storage: FinanceReportStorage,
    financeReportModelMapper: IMapper<JdbcFinanceReportModel, FinanceReport>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcFinanceReportModel, FinanceReport>(storage, financeReportModelMapper, queryAdapter)

@Repository
interface InternalLoanStorage: GenericStorage<JdbcInternalLoanModal, UUID>

@Component
class InternalLoanRepository(
    storage: InternalLoanStorage,
    internalLoanMapper: IMapper<JdbcInternalLoanModal, InternalLoan>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcInternalLoanModal, InternalLoan>(storage, internalLoanMapper, queryAdapter)


@Repository
interface GoalStorage: GenericStorage<JdbcGoalModel, UUID>

@Component
class GoalRepository(
    storage: GoalStorage,
    goalMapper: IMapper<JdbcGoalModel, Goal>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcGoalModel, Goal>(storage, goalMapper, queryAdapter)

@Repository
interface ProfileStorage: GenericStorage<JdbcProfileModel, UUID>

@Component
class ProfileRepository(
    storage: ProfileStorage,
    profileMapper: IMapper<JdbcProfileModel, Profile>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcProfileModel, Profile>(storage, profileMapper, queryAdapter)

@Repository
interface SpendingPeriodTemplateStorage: GenericStorage<JdbcSpendingPeriodTemplateModel, UUID>

@Component
class SpendingPeriodTemplateRepository(
    storage: SpendingPeriodTemplateStorage,
    mapper: IMapper<JdbcSpendingPeriodTemplateModel, SpendingPeriodTemplate>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcSpendingPeriodTemplateModel, SpendingPeriodTemplate>(storage, mapper, queryAdapter)

@Repository
interface SpendingPeriodStorage: GenericStorage<JdbcSpendingPeriodModel, UUID>

@Component
class SpendingPeriodRepository(
    storage: SpendingPeriodStorage,
    mapper: IMapper<JdbcSpendingPeriodModel, SpendingPeriod>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcSpendingPeriodModel, SpendingPeriod>(storage, mapper, queryAdapter)

@Repository
interface AccountSnapshotStorage: GenericStorage<JdbcAccountSnapshotBalance, UUID>
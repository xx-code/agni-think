package dev.auguste.agni_api.infras.persistences

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.AgentSuggestion
import dev.auguste.agni_api.core.entities.BankRegister
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.entities.Currency
import dev.auguste.agni_api.core.entities.Deduction
import dev.auguste.agni_api.core.entities.ExternalTransaction
import dev.auguste.agni_api.core.entities.FinancePrinciple
import dev.auguste.agni_api.core.entities.FinanceReport
import dev.auguste.agni_api.core.entities.Goal
import dev.auguste.agni_api.core.entities.IncomeSource
import dev.auguste.agni_api.core.entities.InternalLoan
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.Notification
import dev.auguste.agni_api.core.entities.Patrimony
import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import dev.auguste.agni_api.core.entities.Profile
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.SpendingPeriod
import dev.auguste.agni_api.core.entities.SpendingPeriodTemplate
import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.core.entities.Transaction
import dev.auguste.agni_api.infras.persistences.jbdc_model.JbdcAccountModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcAgentSuggestionModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcBankRegisterModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcBudgetModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcCategoryModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcCurrencyModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcDeductionModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcExternalTransactionModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcFinancePrincipleModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcFinanceReportModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcGoalModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcIncomeSourceModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcInternalLoanModal
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcInvoiceModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcNotificationModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcPatrimonyModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcPatrimonySnapshotModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcProfileModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcProvisionModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcSavingGoalModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcScheduleInvoiceModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcSpendingPeriodModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcSpendingPeriodTemplateModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcTagModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcTransactionModel
import dev.auguste.agni_api.infras.persistences.query_adapters.IQueryExtendJdbcAdapter
import dev.auguste.agni_api.infras.persistences.query_adapters.JdbcQueryAdapter
import dev.auguste.agni_api.infras.persistences.query_adapters.QueryCategoryExtendJdbcAdapter
import dev.auguste.agni_api.infras.persistences.query_adapters.QueryTagExtendJdbcAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.UUID

@Configuration
class JacksonConfig {
    @Bean
    fun objectMapper(): ObjectMapper {
        return jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .registerKotlinModule()
            .setDateFormat(StdDateFormat())
    }
}

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
    queryExtendJdbcAdapter: QueryCategoryExtendJdbcAdapter,
): JdbcRepository<JdbcCategoryModel, Category>(storage = storage, categoryModelMapper, queryAdapter, queryExtendJdbcAdapter)

// Currency
@Repository
interface CurrencyStorage: GenericStorage<JdbcCurrencyModel, UUID>

@Component
class CurrencyRepository(
    storage: CurrencyStorage,
    currencyModelMapper: IMapper<JdbcCurrencyModel, Currency>,
    queryAdapter: JdbcQueryAdapter,
    queryExtendJdbcAdapter: QueryCategoryExtendJdbcAdapter,
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
    queryExtendAdapter: IQueryExtendJdbcAdapter<JdbcInvoiceModel, Invoice>
): JdbcRepository<JdbcInvoiceModel, Invoice>(storage = storage, invoiceModelMapper, queryAdapter, queryExtendAdapter)

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
    queryExtendAdapter: IQueryExtendJdbcAdapter<JdbcPatrimonySnapshotModel, PatrimonySnapshot>,
    queryAdapter: JdbcQueryAdapter,
): JdbcRepository<JdbcPatrimonySnapshotModel, PatrimonySnapshot>(storage, patrimonySnapshotMapper, queryAdapter, queryExtendAdapter)

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
interface SavingGoalStorage: GenericStorage<JdbcSavingGoalModel, UUID>

@Component
class SavingGoalRepository(
    storage: SavingGoalStorage,
    storageModelMapper: IMapper<JdbcSavingGoalModel, SavingGoal>,
    queryAdapter: JdbcQueryAdapter,
    queryExtendAdapter: IQueryExtendJdbcAdapter<JdbcSavingGoalModel, SavingGoal>,
): JdbcRepository<JdbcSavingGoalModel, SavingGoal>(storage = storage, modelMapper = storageModelMapper, queryAdapter, queryExtendAdapter)

// ScheduleInvoice
@Repository
interface ScheduleInvoiceStorage: GenericStorage<JdbcScheduleInvoiceModel, UUID>

@Component
class ScheduleInvoiceRepository(
    storage: ScheduleInvoiceStorage,
    scheduleModelMapper: IMapper<JdbcScheduleInvoiceModel, ScheduleInvoice>,
    queryAdapter: JdbcQueryAdapter,
    queryExtendAdapter: IQueryExtendJdbcAdapter<JdbcScheduleInvoiceModel, ScheduleInvoice>
): JdbcRepository<JdbcScheduleInvoiceModel, ScheduleInvoice>( storage = storage, scheduleModelMapper, queryAdapter, queryExtendAdapter)

// Tag
@Repository
interface TagStorage: GenericStorage<JdbcTagModel, UUID>

@Component
class TagRepository(
    storage: TagStorage,
    tagModelMapper: IMapper<JdbcTagModel, Tag>,
    queryAdapter: JdbcQueryAdapter,
    queryExtendAdapter: QueryTagExtendJdbcAdapter
): JdbcRepository<JdbcTagModel, Tag>(storage = storage, tagModelMapper, queryAdapter, queryExtendAdapter)

// Transaction
@Repository
interface TransactionStorage: GenericStorage<JdbcTransactionModel, UUID>

@Component
class TransactionRepository(
    storage: TransactionStorage,
    transactionModelMapper: IMapper<JdbcTransactionModel, Transaction>,
    queryAdapter: JdbcQueryAdapter,
    queryExtendAdapter: IQueryExtendJdbcAdapter<JdbcTransactionModel, Transaction>
): JdbcRepository<JdbcTransactionModel, Transaction>( storage = storage, transactionModelMapper, queryAdapter, queryExtendAdapter)

// Budget
@Repository
interface BudgetStorage: GenericStorage<JdbcBudgetModel, UUID>

@Component
class BudgetRepository(
    storage: BudgetStorage,
    budgetModelMapper: IMapper<JdbcBudgetModel, Budget>,
    queryAdapter: JdbcQueryAdapter,
    queryExtendAdapter: IQueryExtendJdbcAdapter<JdbcBudgetModel, Budget>
): JdbcRepository<JdbcBudgetModel, Budget>(storage, budgetModelMapper, queryAdapter, queryExtendAdapter)

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
    queryExtendAdapter: IQueryExtendJdbcAdapter<JdbcAgentSuggestionModel, AgentSuggestion>
): JdbcRepository<JdbcAgentSuggestionModel, AgentSuggestion>(storage, agentSuggestionMapper, queryAdapter, queryExtendAdapter)

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
    queryExtendAdapter: IQueryExtendJdbcAdapter<JdbcExternalTransactionModel, ExternalTransaction>
): JdbcRepository<JdbcExternalTransactionModel, ExternalTransaction>(storage, externalTransactionModelMapper, queryAdapter,queryExtendAdapter)

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
    queryExtendAdapter: IQueryExtendJdbcAdapter<JdbcInternalLoanModal, InternalLoan>,
): JdbcRepository<JdbcInternalLoanModal, InternalLoan>(storage, internalLoanMapper, queryAdapter,queryExtendAdapter)


@Repository
interface GoalStorage: GenericStorage<JdbcGoalModel, UUID>

@Component
class GoalRepository(
    storage: GoalStorage,
    goalMapper: IMapper<JdbcGoalModel, Goal>,
    queryAdapter: JdbcQueryAdapter,
    queryExtendAdapter: IQueryExtendJdbcAdapter<JdbcGoalModel, Goal>
): JdbcRepository<JdbcGoalModel, Goal>(storage, goalMapper, queryAdapter, queryExtendAdapter)

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
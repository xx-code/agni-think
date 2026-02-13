package dev.auguste.agni_api.infras.persistences

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.entities.Currency
import dev.auguste.agni_api.core.entities.Deduction
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.Notification
import dev.auguste.agni_api.core.entities.Patrimony
import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import dev.auguste.agni_api.core.entities.Provisionable
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.core.entities.Transaction
import dev.auguste.agni_api.infras.persistences.jbdc_model.JbdcAccountModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcBudgetModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcCategoryModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcCurrencyModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcDeductionModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcInvoiceModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcNotificationModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcPatrimonyModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcPatrimonySnapshotModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcProvisionableModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcSavingGoalModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcScheduleInvoiceModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcTagModel
import dev.auguste.agni_api.infras.persistences.jbdc_model.JdbcTransactionModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.text.SimpleDateFormat
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
    accountModelMapper: IMapper<JbdcAccountModel, Account>
): JdbcRepository<JbdcAccountModel, Account>(storage = storage, accountModelMapper)

// Category
@Repository
interface CategoryStorage: GenericStorage<JdbcCategoryModel, UUID>

@Component
class CategoryRepository(
    storage: CategoryStorage,
    categoryModelMapper: IMapper<JdbcCategoryModel, Category>
): JdbcRepository<JdbcCategoryModel, Category>(storage = storage, categoryModelMapper)

// Currency
@Repository
interface CurrencyStorage: GenericStorage<JdbcCurrencyModel, UUID>

@Component
class CurrencyRepository(
    storage: CurrencyStorage,
    currencyModelMapper: IMapper<JdbcCurrencyModel, Currency>,
): JdbcRepository<JdbcCurrencyModel, Currency>(storage = storage, currencyModelMapper)

// Deduction
@Repository
interface DeductionStorage: GenericStorage<JdbcDeductionModel, UUID>

@Component
class DeductionRepository(
    storage: DeductionStorage,
    deductionModelMapper: IMapper<JdbcDeductionModel, Deduction>
): JdbcRepository<JdbcDeductionModel, Deduction>(storage = storage, deductionModelMapper)

// Invoice
@Repository
interface InvoiceStorage: GenericStorage<JdbcInvoiceModel, UUID>

@Component
class InvoiceRepository(
    storage: InvoiceStorage,
    invoiceModelMapper: IMapper<JdbcInvoiceModel, Invoice>,
    queryExtendAdapter: IQueryExtendJdbcAdapter<JdbcInvoiceModel, Invoice>
): JdbcRepository<JdbcInvoiceModel, Invoice>(storage = storage, invoiceModelMapper, queryExtendAdapter)

// Notification
@Repository
interface NotificationStorage: GenericStorage<JdbcNotificationModel, UUID>

@Component
class NotificationRepository(
    storage: NotificationStorage,
    notificationModelMapper: IMapper<JdbcNotificationModel, Notification>
): JdbcRepository<JdbcNotificationModel, Notification>(storage = storage, notificationModelMapper)

// Patrimony
@Repository
interface PatrimonyStorage: GenericStorage<JdbcPatrimonyModel, UUID>

@Component
class PatrimonyRepository(
    storage: PatrimonyStorage,
    patrimonyModelMapper: IMapper<JdbcPatrimonyModel, Patrimony>,
): JdbcRepository<JdbcPatrimonyModel, Patrimony>(storage = storage, patrimonyModelMapper)

// PatrimonySnapshot
@Repository
interface PatrimonySnapshotStorage: GenericStorage<JdbcPatrimonySnapshotModel, UUID>

@Component
class PatrimonySnapshotRepository(
    storage: PatrimonySnapshotStorage,
    patrimonySnapshotMapper: IMapper<JdbcPatrimonySnapshotModel, PatrimonySnapshot>,
    queryExtendAdapter: IQueryExtendJdbcAdapter<JdbcPatrimonySnapshotModel, PatrimonySnapshot>
): JdbcRepository<JdbcPatrimonySnapshotModel, PatrimonySnapshot>(storage, patrimonySnapshotMapper, queryExtendAdapter)

// Proisionable
@Repository
interface ProvisionableStorage: GenericStorage<JdbcProvisionableModel, UUID>

@Component
class ProvisionableRepository(
    storage: ProvisionableStorage,
    provisionableModlModelMapper: IMapper<JdbcProvisionableModel, Provisionable>
): JdbcRepository<JdbcProvisionableModel, Provisionable>(storage = storage, provisionableModlModelMapper)

//Saving Goal
@Repository
interface SavingGoalStorage: GenericStorage<JdbcSavingGoalModel, UUID>

@Component
class SavingGoalRepository(
    storage: SavingGoalStorage,
    storageModelMapper: IMapper<JdbcSavingGoalModel, SavingGoal>,
    queryExtendAdapter: IQueryExtendJdbcAdapter<JdbcSavingGoalModel, SavingGoal>
): JdbcRepository<JdbcSavingGoalModel, SavingGoal>(storage = storage, modelMapper = storageModelMapper, queryExtendAdapter)

// ScheduleInvoice
@Repository
interface ScheduleInvoiceStorage: GenericStorage<JdbcScheduleInvoiceModel, UUID>

@Component
class ScheduleInvoiceRepository(
    storage: ScheduleInvoiceStorage,
    scheduleModelMapper: IMapper<JdbcScheduleInvoiceModel, ScheduleInvoice>,
    queryExtendAdapter: IQueryExtendJdbcAdapter<JdbcScheduleInvoiceModel, ScheduleInvoice>
): JdbcRepository<JdbcScheduleInvoiceModel, ScheduleInvoice>( storage = storage, scheduleModelMapper, queryExtendAdapter)

// Tag
@Repository
interface TagStorage: GenericStorage<JdbcTagModel, UUID>

@Component
class TagRepository(
    storage: TagStorage,
    tagModelMapper: IMapper<JdbcTagModel, Tag>
): JdbcRepository<JdbcTagModel, Tag>(storage = storage, tagModelMapper)

// Transaction
@Repository
interface TransactionStorage: GenericStorage<JdbcTransactionModel, UUID>

@Component
class TransactionRepository(
    storage: TransactionStorage,
    transactionModelMapper: IMapper<JdbcTransactionModel, Transaction>,
    queryExtendAdapter: IQueryExtendJdbcAdapter<JdbcTransactionModel, Transaction>
): JdbcRepository<JdbcTransactionModel, Transaction>( storage = storage, transactionModelMapper, queryExtendAdapter)

// Budget
@Repository
interface BudgetStorage: GenericStorage<JdbcBudgetModel, UUID>

@Component
class BudgetRepository(
    storage: BudgetStorage,
    budgetModelMapper: IMapper<JdbcBudgetModel, Budget>,
    queryExtendAdapter: IQueryExtendJdbcAdapter<JdbcBudgetModel, Budget>
): JdbcRepository<JdbcBudgetModel, Budget>(storage, budgetModelMapper, queryExtendAdapter)
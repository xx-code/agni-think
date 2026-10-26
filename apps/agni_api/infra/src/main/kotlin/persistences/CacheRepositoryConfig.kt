package persistences

import adapters.repositories.ICacheStore
import adapters.repositories.IRepository
import domain.convertors.ICacheCodec
import domain.entities.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.time.Duration

@Configuration
class CacheRepositoryConfig {

    private companion object {
        val DEFAULT_TTL: Duration = Duration.ofMinutes(10)
        val LONG_TTL: Duration = Duration.ofHours(1)
        val VOLATILE_TTL: Duration = Duration.ofMinutes(2)
    }

    private fun <T : Entity> cached(
        delegate: IRepository<T>,
        cache: ICacheStore,
        codec: ICacheCodec<T>,
        type: Class<T>,
        ttl: Duration = DEFAULT_TTL,
    ): IRepository<T> = CachedRepository(delegate, cache, codec, type.name, ttl)

    // ---- Reference data (rarely changes): long TTL ----

    @Bean
    @Primary
    fun cachedCurrencyRepository(
        jdbc: CurrencyRepository,
        codec: CurrencyCacheCodec,
        cache: ICacheStore
    ): IRepository<Currency> =
        cached(jdbc, cache, codec, Currency::class.java, LONG_TTL)

    @Bean
    @Primary
    fun cachedCategoryRepository(
        jdbc: CategoryRepository,
        codec: CategoryCacheCodec,
        cache: ICacheStore
    ): IRepository<Category> =
        cached(jdbc, cache, codec, Category::class.java, LONG_TTL)

    @Bean
    @Primary
    fun cachedTagRepository(
        jdbc: TagRepository,
        codec: TagCacheCodec,
        cache: ICacheStore
    ): IRepository<Tag> =
        cached(jdbc, cache, codec, Tag::class.java, LONG_TTL)

    @Bean
    @Primary
    fun cachedFinancePrincipleRepository(
        jdbc: FinancePrincipleRepository,
        codec: FinancePrincipleCacheCodec,
        cache: ICacheStore
    ): IRepository<FinancePrinciple> =
        cached(jdbc, cache, codec, FinancePrinciple::class.java, LONG_TTL)

    @Bean
    @Primary
    fun cachedSpendingPeriodTemplateRepository(
        jdbc: SpendingPeriodTemplateRepository,
        codec: SpendingPeriodTemplateCacheCodec,
        cache: ICacheStore
    ): IRepository<SpendingPeriodTemplate> =
        cached(jdbc, cache, codec, SpendingPeriodTemplate::class.java, LONG_TTL)

    // ---- Regular data: default TTL ----

    @Bean
    @Primary
    fun cachedUserRepository(
        jdbc: UserRepository,
        codec: UserCacheCodec,
        cache: ICacheStore
    ): IRepository<User> =
        cached(jdbc, cache, codec, User::class.java, LONG_TTL)

    @Bean
    @Primary
    fun cachedProfileRepository(
        jdbc: ProfileRepository,
        codec: ProfileCacheCodec,
        cache: ICacheStore
    ): IRepository<Profile> =
        cached(jdbc, cache, codec, Profile::class.java, LONG_TTL)
//
//    @Bean @Primary
//    fun cachedDeductionRepository(jdbc: DeductionRepository, cache: ICacheStore): IRepository<Deduction> =
//        cached(jdbc, cache, Deduction::class.java,DEFAULT_TTL)
//
//
//    @Bean @Primary
//    fun cachedScheduleInvoiceRepository(jdbc: ScheduleInvoiceRepository, cache: ICacheStore): IRepository<ScheduleInvoice> =
//        cached(jdbc, cache, ScheduleInvoice::class.java,DEFAULT_TTL)
//
//    @Bean @Primary
//    fun cachedPatrimonyRepository(jdbc: PatrimonyRepository, cache: ICacheStore): IRepository<Patrimony> =
//        cached(jdbc, cache, Patrimony::class.java,DEFAULT_TTL)
//
//    @Bean @Primary
//    fun cachedPatrimonySnapshotRepository(jdbc: PatrimonySnapshotRepository, cache: ICacheStore): IRepository<PatrimonySnapshot> =
//        cached(jdbc, cache, PatrimonySnapshot::class.java,DEFAULT_TTL)
//
//    @Bean @Primary
//    fun cachedProvisionRepository(jdbc: ProvisionableRepository, cache: ICacheStore): IRepository<Provision> =
//        cached(jdbc, cache, Provision::class.java,DEFAULT_TTL)
//
//    @Bean @Primary
//    fun cachedFundRepository(jdbc: SavingGoalRepository, cache: ICacheStore): IRepository<Fund> =
//        cached(jdbc, cache, Fund::class.java,DEFAULT_TTL)
//
//    @Bean @Primary
//    fun cachedGoalRepository(jdbc: GoalRepository, cache: ICacheStore): IRepository<Goal> =
//        cached(jdbc, cache, Goal::class.java,DEFAULT_TTL)
//
//    @Bean @Primary
//    fun cachedBudgetRepository(jdbc: BudgetRepository, cache: ICacheStore): IRepository<Budget> =
//        cached(jdbc, cache, Budget::class.java,DEFAULT_TTL)
//
//    @Bean @Primary
//    fun cachedIncomeSourceRepository(jdbc: IncomeSourceRepository, cache: ICacheStore): IRepository<IncomeSource> =
//        cached(jdbc, cache, IncomeSource::class.java, DEFAULT_TTL)
//
//    @Bean @Primary
//    fun cachedBankRegisterRepository(jdbc: BankRegisterRepository, cache: ICacheStore): IRepository<BankRegister> =
//        cached(jdbc, cache, BankRegister::class.java, DEFAULT_TTL)
//
//    @Bean @Primary
//    fun cachedFinanceReportRepository(jdbc: FinanceReportRepository, cache: ICacheStore): IRepository<FinanceReport> =
//        cached(jdbc, cache, FinanceReport::class.java, DEFAULT_TTL)
//
//    @Bean @Primary
//    fun cachedSpendingPeriodRepository(jdbc: SpendingPeriodRepository, cache: ICacheStore): IRepository<SpendingPeriod> =
//        cached(jdbc, cache, SpendingPeriod::class.java, DEFAULT_TTL)
//
//    // ---- Volatile data: short TTL ----
//
//    @Bean @Primary
//    fun cachedInvoiceRepository(jdbc: InvoiceRepository, cache: ICacheStore): IRepository<Invoice> =
//        cached(jdbc, cache, Invoice::class.java, VOLATILE_TTL)

//    @Bean @Primary
//    fun cachedAccountRepository(jdbc: AccountRepository, codec: AccountCacheCodec, cache: ICacheStore): IRepository<Account> =
//        cached(jdbc, cache, codec, Account::class.java, VOLATILE_TTL)
//
//    @Bean @Primary
//    fun cachedTransactionRepository(jdbc: TransactionRepository, cache: ICacheStore): IRepository<Transaction> =
//        cached(jdbc, cache, Transaction::class.java, VOLATILE_TTL)
//
//    @Bean @Primary
//    fun cachedExternalTransactionRepository(jdbc: ExternalBankRegisterRepository, cache: ICacheStore): IRepository<ExternalTransaction> =
//        cached(jdbc, cache, ExternalTransaction::class.java, VOLATILE_TTL)
//
//    @Bean @Primary
//    fun cachedNotificationRepository(jdbc: NotificationRepository, cache: ICacheStore): IRepository<Notification> =
//        cached(jdbc, cache, Notification::class.java, VOLATILE_TTL)
//
//    @Bean @Primary
//    fun cachedAgentSuggestionRepository(jdbc: AgentSuggestionRepository, cache: ICacheStore): IRepository<AgentSuggestion> =
//        cached(jdbc, cache, AgentSuggestion::class.java, VOLATILE_TTL)
}
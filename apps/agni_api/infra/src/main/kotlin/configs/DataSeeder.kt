package configs

import adapters.dto.AccountSnapshotBalanceInput
import adapters.dto.ScheduleRepeaterInput
import adapters.repositories.IUnitOfWork
import domain.SAVING_CATEGORY_ID
import facades.DataSeederDependencies
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID
import domain.entities.*
import domain.enums.DepreciationType
import domain.enums.FundType
import domain.enums.IncomeSourceFrequencyType
import domain.enums.IncomeSourceType
import domain.enums.InvoiceModuleLinkerType
import domain.enums.InvoiceStatusType
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceType
import domain.enums.PeriodType
import domain.enums.ProvisionType
import domain.enums.ScheduleInvoiceModuleLinkerType
import domain.value_objects.CheckingAccountDetail
import domain.value_objects.InvoiceModuleLinker
import domain.value_objects.ProvisionDepreciateCriteria
import domain.value_objects.SavingAccountDetail
import domain.value_objects.ScheduleInvoiceModuleLinker
import domain.value_objects.Scheduler
import domain.value_objects.SchedulerRecurrence
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

@Component
class DataSeeder(
    private val dep: DataSeederDependencies,
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val uow: IUnitOfWork
) {
    suspend fun reset(userId: UUID) {
        // 1. Suppression des transactions liées aux factures de l'utilisateur
        jdbcTemplate.jdbcTemplate.update(
            """
        DELETE FROM transactions 
        WHERE invoice_id IN (SELECT invoice_id FROM invoices WHERE user_id = ?)
        """,
            userId
        )

        jdbcTemplate.jdbcTemplate.update(
            """
        DELETE FROM patrimony_snapshots
        WHERE patrimony_id IN (SELECT patrimony_id FROM patrimonies WHERE user_id = ?)
        """,
            userId
        )

        jdbcTemplate.jdbcTemplate.update(
            """
        DELETE FROM patrimony_accounts
        WHERE patrimony_id IN (SELECT account_id FROM accounts WHERE user_id = ?)
        """,
            userId
        )

        jdbcTemplate.jdbcTemplate.update(
            """
        DELETE FROM patrimony_accounts
        WHERE patrimony_id IN (SELECT account_id FROM accounts WHERE user_id = ?)
        """,
            userId
        )

        // 2. Liste des tables possédant directement la colonne user_id
        // L'ordre respecte les contraintes de clés étrangères (enfants d'abord, parents ensuite)
        val tablesWithUserId = listOf(
            "account_snapshot_balances",
            "external_transactions",
            "bank_registers",
            "invoices", // Supprimé après 'transactions'
            "schedule_transactions",
            "spending_periods",
            "spending_period_templates",
            "provisions",
            "goals",
            "funds",
            "income_sources",
            "finance_reports",
            "finance_principles",
            "agent_suggestions",
            "notifications",
            "patrimonies",
            "budgets",
            "categories",
            "tags",
            "deductions",
            "currencies",
            "accounts"
        )

        for (table in tablesWithUserId) {
            jdbcTemplate.jdbcTemplate.update(
                "DELETE FROM $table WHERE user_id = ?",
                userId
            )
        }
    }

    suspend fun seed(userId: UUID) {
        uow.execute {
            reset(userId)

            // 1. Creation Account
            val mainAccount = Account.mock(
                userId = userId,
                title = "Compte Courant Principal",
                balance = 4500.0,
                color = Color("#10a096"),
                detail = CheckingAccountDetail(500.0)
            )
            val savingAccount = Account.mock(
                userId = userId,
                title = "Compte Épargne",
                balance = 12000.0,
                color = Color("#3b82f6"),
                detail = SavingAccountDetail(1000.0)
            )
            dep.accountRepo.create(mainAccount)
            dep.accountRepo.create(savingAccount)

            // 2. Ref Category
            val catIncome = Category(title = "Revenus", icon = "i-lucide-briefcase", color = Color("#22c55e"), userId = userId)
            val catHousing = Category(title = "Logement", icon = "i-lucide-home", color = Color("#3b82f6"), userId = userId)
            val catUtilities = Category(title = "Services & Factures", icon = "i-lucide-zap", color = Color("#eab308"), userId = userId)
            val catGroceries = Category(title = "Alimentation", icon = "i-lucide-shopping-cart", color = Color("#ec4899"), userId = userId)
            val catLeisure = Category(title = "Loisirs & Sorties", icon = "i-lucide-film", color = Color("#8b5cf6"), userId = userId)
            val catTransport = Category(title = "Transports", icon = "i-lucide-truck", color = Color("#06b6d4"), userId = userId)
            val catSavings = Category(title = "Épargne", icon = "i-lucide-piggy-bank", color = Color("#10b981"), userId = userId)

            listOf(catIncome, catHousing, catUtilities, catGroceries, catLeisure, catTransport, catSavings).forEach {
                dep.categoryRepo.create(it)
            }

            // 3. Tag ref
            val tagEssential = Tag(value = "essentiel", color = Color("#ef4444"), userId = userId)
            val tagRecurring = Tag(value = "recurrent", color = Color("#64748b"), userId = userId)
            val tagPleasure = Tag(value = "plaisir", color = Color("#10b981"), userId = userId)
            val tagSalary = Tag(value = "salaire", color = Color("#22c55e"), userId = userId)

            listOf(tagEssential, tagRecurring, tagPleasure, tagSalary).forEach {
                dep.tagRepo.create(it)
            }

            // 4. Creation Budgets
            val budgetGroceries = Budget(
                userId = userId,
                title = "Budget Alimentation",
                target = 600.0,
                scheduler = Scheduler(
                    date = LocalDateTime.now().withDayOfMonth(1),
                    repeater = SchedulerRecurrence(PeriodType.MONTH, 1)
                )
            )
            val budgetLeisure = Budget(
                userId = userId,
                title = "Budget Loisirs",
                target = 350.0,
                scheduler = Scheduler(
                    date = LocalDateTime.now().withDayOfMonth(1),
                    repeater = SchedulerRecurrence(PeriodType.MONTH, 1)
                )
            )
            dep.budgetRepo.create(budgetGroceries)
            dep.budgetRepo.create(budgetLeisure)

            // 5. Creation Fund
            val emergencyFund = Fund(
                userId = userId,
                title = "Fonds d'urgence",
                description = "Épargne de précaution 3 mois",
                target = 10000.0,
                balance = 5000.0,
                type = FundType.EMERGENCY,
                accountId = savingAccount.id
            )
            val amortizationFund = Fund(
                userId = userId,
                title = "Fonds Amortissement Véhicule",
                description = "Pour entretien et remplacement",
                target = 5000.0,
                balance = 0.0,
                type = FundType.AMORTIZATION,
                accountId = null
            )
            dep.fundRepo.create(emergencyFund)
            val dateSave = LocalDateTime.now()
            val invSave = Invoice(
                userId = userId,
                accountId = savingAccount.id,
                status = InvoiceStatusType.COMPLETED,
                movement = InvoiceMovementType.DEBIT,
                type = InvoiceType.OTHER,
                date = dateSave,
                moduleLinkers = mutableListOf(InvoiceModuleLinker(emergencyFund.id, InvoiceModuleLinkerType.FUND)),
            )
            dep.invoiceRepo.create(invSave)
            dep.transactionRepo.create(Transaction(
                invoiceId = invSave.id,
                categoryId = SAVING_CATEGORY_ID,
                amount = 5000.0,
                description =" Argent plan d'epargne ${emergencyFund.title}"
            ))
            dep.fundRepo.create(amortizationFund)
            val savAcc = dep.accountRepo.get(savingAccount.id)
            if (savAcc != null) {
                savAcc.balance -= 5000.0
                dep.accountRepo.update(savAcc)
                dep.snapshotRepository.makeSnapshot(AccountSnapshotBalanceInput(userId, savAcc.id, savAcc.balance, dateSave))
            }

            // 6. Creation IncomeSource Source de Revenu (IncomeSource)
            val incomeSource = IncomeSource(
                userId = userId,
                title = "Emploi Principal (Salaire)",
                type = IncomeSourceType.SALARY,
                payFrequency = IncomeSourceFrequencyType.MONTHLY,
                reliabilityLevel = 100,
                startDate = LocalDate.now().minusMonths(6),
                taxRate = 15.0,
                otherRate = 5.0,
                linkedAccountId = mainAccount.id,
                annualGrossAmount = 60000.0
            )
            dep.incomeSourceRepo.create(incomeSource)

            // Spending template period
            dep.spendingPeriodTemplateRepo.create(SpendingPeriodTemplate(
                userId = userId,
                startDate = LocalDate.now().minusMonths(6).with(TemporalAdjusters.firstDayOfMonth()),
                recurrence = SchedulerRecurrence(PeriodType.MONTH, 1),
                targetBudgetIds = setOf(budgetLeisure.id, budgetGroceries.id),
                isActive = true
            ))

            // 7. Creation Provision
            val provision = Provision(
                userId = userId,
                title = "Ordinateur Portable Pro",
                costHT = 1200.0,
                costTTC = 1380.0,
                isPatrimony = true,
                acquisitionDate = LocalDate.now().minusMonths(2),
                expectedLifespanMonth = 24,
                depreciationCriteria = mutableListOf(
                    ProvisionDepreciateCriteria(
                        "Linéaire",
                        "Dépréciation standard",
                        DepreciationType.STRAIGHT_LINE,
                        50.0
                    )
                ),
                floorValue = 100.0,
                type = ProvisionType.DEPRECIATE
            )
            dep.provisionRepo.create(provision)

            // 8. Creation ScheduleInvoice
            val scheduleRent = ScheduleInvoice(
                userId = userId,
                title = "Loyer Appartement (Planifié)",
                accountId = mainAccount.id,
                type = InvoiceType.FIXED_COST,
                amount = 1300.0,
                scheduler = Scheduler(
                    date = LocalDateTime.now().withDayOfMonth(1).plusMonths(1),
                    repeater = SchedulerRecurrence(PeriodType.MONTH, 1)
                ),
                categoryId = catHousing.id,
                moduleLinker = null,
                freezeScheduler = null,
                tagIds = mutableSetOf(tagEssential.id, tagRecurring.id)
            )
            dep.scheduleInvoiceRepo.create(scheduleRent)
            val scheduleIncome = ScheduleInvoice(
                userId = userId,
                title = incomeSource.title,
                accountId = incomeSource.linkedAccountId!!,
                type = InvoiceType.INCOME,
                amount = incomeSource.getEstimateNextNetAmount(),
                scheduler = Scheduler(
                    date = incomeSource.getEstimateNextDate().atStartOfDay(),
                    repeater = SchedulerRecurrence(
                        period = when(incomeSource.payFrequency) {
                            IncomeSourceFrequencyType.BIWEEKLY -> PeriodType.WEEK
                            IncomeSourceFrequencyType.MONTHLY -> PeriodType.MONTH
                            IncomeSourceFrequencyType.YEARLY -> PeriodType.YEAR
                            else -> PeriodType.MONTH
                        },
                        interval = when(incomeSource.payFrequency) {
                            IncomeSourceFrequencyType.BIWEEKLY -> 2
                            else -> 1
                        }
                    )
                ),
                categoryId = catIncome.id,
                moduleLinker = ScheduleInvoiceModuleLinker(incomeSource.id, ScheduleInvoiceModuleLinkerType.INCOME_SOURCE),
                freezeScheduler = null,
                isPause = false,
                isFreeze = false,
                endDate = incomeSource.endDate?.atStartOfDay()
            )
            dep.scheduleInvoiceRepo.create(scheduleIncome)

            // 9. Simulation
            val endDate = LocalDate.now()
            val startDate = endDate.minusMonths(6)
            var currentMonthDate = startDate.withDayOfMonth(1)

            while (!currentMonthDate.isAfter(endDate)) {
                // Salaire (1er du mois)
                val dateInc = currentMonthDate.withDayOfMonth(1).atTime(9, 0)
                if (!dateInc.toLocalDate().isAfter(endDate)) {
                    val invInc = Invoice(
                        userId = userId,
                        accountId = mainAccount.id,
                        status = InvoiceStatusType.COMPLETED,
                        movement = InvoiceMovementType.CREDIT,
                        type = InvoiceType.INCOME,
                        date = dateInc,
                        moduleLinkers = mutableListOf(
                            InvoiceModuleLinker(
                                incomeSource.id,
                                InvoiceModuleLinkerType.INCOME_SOURCE
                            )
                        )
                    )
                    dep.invoiceRepo.create(invInc)
                    dep.transactionRepo.create(Transaction(
                        invoiceId = invInc.id,
                        categoryId = catIncome.id,
                        amount = 4000.0,
                        tagIds = mutableSetOf(tagSalary.id, tagRecurring.id),
                        description = "Salaire Mensuel Net"
                    ))
                    val acc = dep.accountRepo.get(mainAccount.id)
                    if (acc != null) {
                        acc.balance += 4000.0
                        dep.accountRepo.update(acc)
                        dep.snapshotRepository.makeSnapshot(AccountSnapshotBalanceInput(userId, acc.id, acc.balance, dateInc))
                    }
                }

                // Loyer (1er du mois)
                val dateRent = currentMonthDate.withDayOfMonth(1).atTime(10, 0)
                if (!dateRent.toLocalDate().isAfter(endDate)) {
                    val invRent = Invoice(
                        userId = userId,
                        accountId = mainAccount.id,
                        status = InvoiceStatusType.COMPLETED,
                        movement = InvoiceMovementType.DEBIT,
                        type = InvoiceType.FIXED_COST,
                        date = dateRent
                    )
                    dep.invoiceRepo.create(invRent)
                    dep.transactionRepo.create(Transaction(
                        invoiceId = invRent.id,
                        categoryId = catHousing.id,
                        amount = 1300.0,
                        tagIds = mutableSetOf(tagEssential.id, tagRecurring.id),
                        description = "Loyer Appartement"
                    ))
                    val acc = dep.accountRepo.get(mainAccount.id)
                    if (acc != null) {
                        acc.balance -= 1300.0
                        dep.accountRepo.update(acc)
                        dep.snapshotRepository.makeSnapshot(AccountSnapshotBalanceInput(userId, acc.id, acc.balance, dateInc))
                    }
                }

                // Électricité (2 du mois)
                val dateUtil = currentMonthDate.withDayOfMonth(2).atTime(10, 0)
                if (!dateUtil.toLocalDate().isAfter(endDate)) {
                    val invUtil = Invoice(
                        userId = userId,
                        accountId = mainAccount.id,
                        status = InvoiceStatusType.COMPLETED,
                        movement = InvoiceMovementType.DEBIT,
                        type = InvoiceType.FIXED_COST,
                        date = dateUtil
                    )
                    dep.invoiceRepo.create(invUtil)
                    dep.transactionRepo.create(Transaction(
                        invoiceId = invUtil.id,
                        categoryId = catUtilities.id,
                        amount = 120.0,
                        tagIds = mutableSetOf(tagEssential.id, tagRecurring.id),
                        description = "Hydro-Québec / Électricité"
                    ))
                    val acc = dep.accountRepo.get(mainAccount.id)
                    if (acc != null) {
                        acc.balance -= 120.0
                        dep.accountRepo.update(acc)
                        dep.snapshotRepository.makeSnapshot(AccountSnapshotBalanceInput(userId, acc.id, acc.balance, dateUtil))
                    }
                }

                // Épicerie (3 du mois)
                val dateGroceries = currentMonthDate.withDayOfMonth(3).atTime(14, 0)
                if (!dateGroceries.toLocalDate().isAfter(endDate)) {
                    val invGroceries = Invoice(
                        userId = userId,
                        accountId = mainAccount.id,
                        status = InvoiceStatusType.COMPLETED,
                        movement = InvoiceMovementType.DEBIT,
                        type = InvoiceType.VARIABLE_COST,
                        date = dateGroceries
                    )
                    dep.invoiceRepo.create(invGroceries)
                    dep.transactionRepo.create(Transaction(
                        invoiceId = invGroceries.id,
                        categoryId = catGroceries.id,
                        amount = 145.50,
                        tagIds = mutableSetOf(tagEssential.id),
                        budgetIds = mutableSetOf(budgetGroceries.id),
                        description = "Supermarché Metro"
                    ))
                    val acc = dep.accountRepo.get(mainAccount.id)
                    if (acc != null) {
                        acc.balance -= 145.50
                        dep.accountRepo.update(acc)
                        dep.snapshotRepository.makeSnapshot(AccountSnapshotBalanceInput(userId, acc.id, acc.balance, dateGroceries))
                    }
                }

                // Loisirs / Restaurant (7 du mois)
                val dateLeisure = currentMonthDate.withDayOfMonth(7).atTime(19, 0)
                if (!dateLeisure.toLocalDate().isAfter(endDate)) {
                    val invLeisure = Invoice(
                        userId = userId,
                        accountId = mainAccount.id,
                        status = InvoiceStatusType.COMPLETED,
                        movement = InvoiceMovementType.DEBIT,
                        type = InvoiceType.VARIABLE_COST,
                        date = dateLeisure
                    )
                    dep.invoiceRepo.create(invLeisure)
                    dep.transactionRepo.create(Transaction(
                        invoiceId = invLeisure.id,
                        categoryId = catLeisure.id,
                        amount = 75.0,
                        tagIds = mutableSetOf(tagPleasure.id),
                        budgetIds = mutableSetOf(budgetLeisure.id),
                        description = "Restaurant Sortie Amis"
                    ))
                    val acc = dep.accountRepo.get(mainAccount.id)
                    if (acc != null) {
                        acc.balance -= 75.0
                        dep.accountRepo.update(acc)
                        dep.snapshotRepository.makeSnapshot(AccountSnapshotBalanceInput(userId, acc.id, acc.balance, dateLeisure))
                    }
                }

                currentMonthDate = currentMonthDate.plusMonths(1)
            }
        }
    }
}
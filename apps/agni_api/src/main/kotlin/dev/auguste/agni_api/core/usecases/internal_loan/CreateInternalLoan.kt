package dev.auguste.agni_api.core.usecases.internal_loan

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.adapters.repositories.query_extend.ComparatorType
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryDateComparator
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryInternalLoanExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryInvoiceExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryScheduleInvoiceExtend
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.InternalLoan
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.entities.roundTo
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IInnerUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.internal_loan.dto.CreateInternalLoanInput
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetInvoiceOutput
import dev.auguste.agni_api.core.value_objects.CreditCardAccountDetail
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.abs

class CreateInternalLoan(
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val accountRepo: IRepository<Account>,
    private val createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
    private val invoiceRepo: IRepository<Invoice>,
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val getInvoice: IUseCase<UUID, GetInvoiceOutput>,
    private val unitOfWork: IUnitOfWork
): IUseCase<CreateInternalLoanInput, CreatedOutput> {
    override fun execAsync(input: CreateInternalLoanInput): CreatedOutput {
        return unitOfWork.execute {
            val account = accountRepo.get(input.fundSourceId) ?: throw DomainException.NotFound.Account(input.fundSourceId)
            val creditAccount = accountRepo.get(input.creditTargetId) ?: throw DomainException.NotFound.Account(input.creditTargetId)
            val internalLoans = internalLoanRepo.getAll(query = QueryFilter(queryAll = true), QueryInternalLoanExtend(fundSourceId = input.fundSourceId))
            var currentLoanBalance = 0.0
            if (internalLoans.items.isNotEmpty()) {
                internalLoans.items.forEach { internalLoanItem ->
                    currentLoanBalance += getInvoice.execAsync(internalLoanItem.invoiceId).total
                }
            }

//            val invoices = invoiceRepo.getAll(QueryFilter(queryAll = true), QueryInvoiceExtend(status = InvoiceStatusType.PENDING))
//            val otherPendingInvoices = invoices.items.filter { internalLoans.items.map { loan -> loan.invoiceId }.contains(it.id) }
//            if (otherPendingInvoices.isNotEmpty()) {
//                throw DomainException.BusinessLogic.InternalLoanAllPendingMustBeReady()
//            }

            val scheduleInvoice = scheduleInvoiceRepo.getAll(QueryFilter(queryAll = true),
                QueryScheduleInvoiceExtend(
                    type = InvoiceType.INCOME,
                    comparatorDueDate = QueryDateComparator(input.dueDate.atStartOfDay() , ComparatorType.LesserOrEquals)
                )
            )

            val accountType = account.detail.getType()
            if (accountType != AccountType.CHECKING && accountType != AccountType.SAVING)
                throw DomainException.BusinessLogic.InternalLoanAccountNotAllowForCollateral()

            if (creditAccount.detail.getType() != AccountType.CREDIT_CARD)
                throw DomainException.BusinessLogic.InternalLoanBadAccountCredit()

            val creditCardDetail = (creditAccount.detail as CreditCardAccountDetail)
            val creditUtilization = if (creditCardDetail.creditLimit > 0) {
                ((abs(creditAccount.balance) / creditCardDetail.creditLimit).roundTo(2)) * 100
            } else {
                0.0
            }

            var nextPaymentDate = creditCardDetail.invoiceDate
            while (nextPaymentDate.isBefore(LocalDate.now())) {
                nextPaymentDate = nextPaymentDate.plusMonths(1)
            }

            val resCreateInvoice = createInvoice.execInnerAsync(input.invoiceInput)
            val newInvoice = getInvoice.execAsync(resCreateInvoice.newId)

//            val confidence = calculateLoanConfidence(
//                newInvoice.date.toLocalDate(),
//                nextPaymentDate,
//                account.balance,
//                (newInvoice.total + currentLoanBalance),
//                creditUtilization,
//                scheduleInvoice.items
//            )
//            if (confidence < 80.0) {
//                throw DomainException.BusinessLogic.InternalLoanBadConfidenceScore(confidence)
//            }

            val internalLoan = InternalLoan(
                creditTargetId = input.creditTargetId,
                invoiceId = resCreateInvoice.newId,
                fundSourceId = input.fundSourceId,
                dueDate = input.dueDate,
            )

            internalLoanRepo.create(internalLoan)

            CreatedOutput(internalLoan.id)
        }
    }



    private fun calculateLoanConfidence(
        purchaseDate: LocalDate,
        nextStatementDate: LocalDate,
        savingBalance: Double,
        loanAmount: Double,
        creditUtilization: Double,
        scheduleIncomes: List<ScheduleInvoice>
    ): Double {
        val realDueDate = nextStatementDate.plusDays(18)
        val daysAvailable = ChronoUnit.DAYS.between(purchaseDate, realDueDate)

        // 1. Score de Temps (Période de grâce) - Poids 20%
        val timeScore = if (daysAvailable >= 30) 1.0 else (daysAvailable.toDouble() / 30.0).coerceAtLeast(0.0)

        // 2. Score d'Épargne (Collatéral) - Poids 40%
        val maxSavingThreshold = savingBalance * 0.1
        val savingScore = if (loanAmount <= maxSavingThreshold) 1.0 else (maxSavingThreshold / loanAmount).coerceAtLeast(0.0)

        // 3. Score de Revenu Futur - Poids 30%
        val futureIncome = scheduleIncomes.sumOf { it.amount * (it.scheduler.repeater?.computeOccurrences(purchaseDate, realDueDate) ?: 1) }
        val maxIncomeThreshold = futureIncome * 0.10 // Monté à 10% pour le calcul de score
        val incomeScore = if (loanAmount <= maxIncomeThreshold) 1.0 else (maxIncomeThreshold / loanAmount).coerceAtLeast(0.0)

        // 4. Score de Crédit (Utilisation) - Poids 10%
        val creditScore = if (creditUtilization <= 10) 1.0 else (10.0 / creditUtilization).coerceIn(0.0, 1.0)

        // Calcul final pondéré
        val totalScore = (timeScore * 20) + (savingScore * 40) + (incomeScore * 30) + (creditScore * 10)
        return totalScore // Score sur 100
    }
}
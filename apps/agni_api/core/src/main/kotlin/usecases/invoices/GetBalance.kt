package usecases.invoices

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Invoice
import domain.entities.User
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import domain.exceptions.NotFoundException

class GetBalance(
    private val userRepo: IRepository<User>,
    private val invoiceRepo: IRepository<Invoice>,
    private val getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>
): UseCase<GetBalanceInput, GetBalanceOutput>() {
    override suspend fun process(input: GetBalanceInput): GetBalanceOutput {
        userRepo.get(input.userId) ?: throw NotFoundException.SingleEntity(input.userId, "user")

        val conditionInvoice = QueryExtendBuilder<Invoice>()
        conditionInvoice.addCondition("userId", QueryComparator.Equal, input.userId)
        conditionInvoice.addCondition("accountId", QueryComparator.In, input.accountIds)
            .addCondition("date", QueryComparator.GreaterOrEquals, input.startDate)
            .addCondition("date", QueryComparator.LesserOrEquals, input.endDate)
            .addCondition("type", QueryComparator.In, input.types?.map { it.value }?.toSet())
            .addCondition("isFreeze", QueryComparator.Equal, input.isFreeze)
            .addCondition("statusType", QueryComparator.Equal, input.status?.value)
            .addCondition("movementType", QueryComparator.Equal, input.movement)

        val invoices = invoiceRepo.getAll(QueryFilter.queryAll(), conditionInvoice)

        val invoiceTransactions = getInvoiceTransactions.processDirect(GetInvoiceTransactionsInput(
            invoiceIds = invoices.items.map { it.id }.toSet(),
            categoryIds = input.categoryIds,
            tagIds = input.tagIds,
            budgetIds = input.budgetIds,
            minAmount = input.minAmount,
            maxAmount = input.maxAmount,
            doRemoveSpecialCategory = input.removeSystemCategory == true && input.categoryIds.isNullOrEmpty(),
        ))

        val creditInvoiceIds = invoices.items.filter { it.movement == InvoiceMovementType.CREDIT }.map { it.id }
        val debitInvoiceIds = invoices.items.filter { it.movement == InvoiceMovementType.DEBIT }.map { it.id }

        val income = invoiceTransactions.filter { creditInvoiceIds.contains(it.invoiceId) }.sumOf { it.total }
        val spend = invoiceTransactions.filter { debitInvoiceIds.contains(it.invoiceId) }.sumOf { it.total }

        val balance = income - spend

        return GetBalanceOutput(
            balance = balance,
            income = income,
            spend = spend
        )
    }
}
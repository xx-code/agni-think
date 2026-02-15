package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.SAVING_CATEGORY_ID
import dev.auguste.agni_api.core.TRANSFERT_CATEGORY_ID
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryInvoiceExtend
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.Transaction
import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.facades.InvoiceDependencies
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput

class GetBalance(
    private val invoiceRepo: IRepository<Invoice>,
    private val getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>
): IUseCase<GetBalanceInput, GetBalanceOutput> {
    override fun execAsync(input: GetBalanceInput): GetBalanceOutput {
        val invoices = invoiceRepo.getAll(QueryFilter(0, 0, true), QueryInvoiceExtend(
            accountIds = input.accountIds,
            startDate = input.startDate,
            endDate = input.endDate,
            types = input.types,
            status = input.status.let { InvoiceStatusType.COMPLETED },
            isFreeze = input.isFreeze,
            mouvementType = input.mouvement
        ))

        val invoiceTransactions = getInvoiceTransactions.execAsync(GetInvoiceTransactionsInput(
            invoiceIds = invoices.items.map { it.id }.toSet(),
            categoryIds = input.categoryIds,
            tagIds = input.tagIds,
            budgetIds = input.budgetIds,
            minAmount = input.minAmount,
            maxAmount = input.maxAmount,
            doRemoveSpecialCategory = input.removeSystemCategory == true && input.categoryIds.isNullOrEmpty(),
        ))

        val creditInvoiceIds = invoices.items.filter { it.mouvementType == InvoiceMouvementType.CREDIT }.map { it.id }
        val debitInvoiceIds = invoices.items.filter { it.mouvementType == InvoiceMouvementType.DEBIT }.map { it.id }

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
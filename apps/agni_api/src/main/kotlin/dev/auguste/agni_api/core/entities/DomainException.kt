package dev.auguste.agni_api.core.entities

import java.util.UUID
import kotlin.math.roundToInt

sealed class DomainException(val code: String, message: String): Exception(message) {
    sealed class NotFound(code: String, message: String): DomainException(code, message) {
        class Account(id: UUID) : NotFound("ACCOUNT_NOT_FOUND", "Le compte est introuvable $id")
        class Category(id: UUID) : NotFound("CATEGORY_NOT_FOUND", "La categorie est introuvable $id")
        class SomeAccounts(ids: Set<UUID>) : NotFound("SOME_ACCOUNT_NOT_FOUND", "Un ou des comptes dans cette liste [${ids.joinToString(", ")}] sont introuvable")
        class Provisionable(id: UUID) : NotFound("PROVISIONABLE_NOT_FOUND", "Provisionable not found $id")
        class Patrimony(id: UUID) : NotFound("PATRIMONY_NOT_FOUND", "Patrimony not found $id")
        class FinanceReport(id: UUID) : NotFound("FINANCE_REPORT_NOT_FOUND", "FinanceReport not found $id")
        class Currency(id: UUID) : NotFound("CURRENCY_NOT_FOUND", "Currency not found $id")
        class Notification(id: UUID) : NotFound("NOTIFICATION_NOT_FOUND", "Notification not found $id")
        class SavingGoal(id: UUID) : NotFound("SAVING_GOAL_NOT_FOUND", "Saving goal not found $id")
        class SomeSavingGoals(ids: Set<UUID>) : NotFound("SOME_SAVING_GOAL_NOT_FOUND", "Un ou des epargnes dans cette liste [${ids.joinToString(", ")}] sont introuvable")
        class IncomeSource(id: UUID) : NotFound("INCOME_SOURCE_NOT_FOUND", "Income source not found $id")
        class Budget(id: UUID) : NotFound("BUDGET_NOT_FOUND", "Budget not found $id")
        class SomeBudgets(ids: Set<UUID>) : NotFound("SOME_BUDGET_NOT_FOUND", "Un ou des budgets dans cette liste [${ids.joinToString(", ")}] sont introuvable")
        class Tag(idOrName: UUID) : NotFound("TAG_NOT_FOUND", "Tag not found $idOrName")
        class SomeTags(ids: Set<UUID>) : NotFound("SOME_TAG_NOT_FOUND", "Un ou des tag dans cette liste [${ids.joinToString(", ")}] sont introuvable")
        class ExternalTransaction(id: UUID) : NotFound("EXTERNAL_TRANSACTION_NOT_FOUND", "External transaction not found $id")
        class Deduction(id: UUID) : NotFound("DEDUCTION_NOT_FOUND", "Deduction not found $id")
        class SomeDeductions(ids: Set<UUID>) : NotFound("SOME_DEDUCTION_NOT_FOUND", "Un ou des deductions dans cette liste [${ids.joinToString(", ")}] sont introuvable")
        class Snapshot(id: UUID) : NotFound("SNAPSHOT_NOT_FOUND", "Snapshot not found $id")
        class BankRegister(id: UUID) : NotFound("BANK_REGISTER_NOT_FOUND", "Bank Register not found $id")
        class Invoice(id: UUID) : NotFound("INVOICE_NOT_FOUND", "Invoice not found $id")
        class FinancePrinciple(id: UUID) : NotFound("FINANCE_PRINCIPLE_NOT_FOUND", "FinancePrinciple not found $id")
        class AgentSuggestion(id: UUID): NotFound("AGENT_SUGGEST_NOT_FOUND", "La suggestion de l'agent $id est introuvable")
        class InternalLoan(id: UUID): NotFound("INTERNAL_LOAN_NOT_FOUND", "Pret personnel est introuvable $id")
        class InternalLoanFreezeInvoice(id: UUID): NotFound("INTERNAL_LOAN_FREEZE_NOT_FOUND", "La facture freeze pour le Pret personnel est introuvable $id")
        class Transaction(id: UUID) : NotFound("TRANSACTION_NOT_FOUND", "Transaction not found $id")
        class ScheduleInvoice(id: UUID): NotFound("SCHEDULE_IN_VOICE_NOT_FOUND", "ScheduleInvoice $id est introuvable")
    }

    sealed class AlreadyExist(code: String, message: String): DomainException(code, message) {
        class Account(name: String) : AlreadyExist("ACCOUNT_ALREADY_EXISTS", "Le compte existe deja $name")
        class Category(name: String): AlreadyExist("CATEGORY_ALREADY_EXISTS", "Categorie exist deja $name")
        class Patrimony(name: String) : AlreadyExist("PATRIMONY_ALREADY_EXISTS", "Patrimony name already exists $name")
        class Budget(name: String) : AlreadyExist("BUDGET_ALREADY_EXISTS", "Budget name already exists $name")
        class IncomeSource(name: String) : AlreadyExist("INCOME_SOURCE_ALREADY_EXISTS", "Income source already exists $name")
        class Currency(name: String) : AlreadyExist("CURRENCY_ALREADY_EXISTS", "Currency already exists $name")
        class Tag(name: String) : AlreadyExist("TAG_ALREADY_EXISTS", "Tag with name already exists $name")
        class Deduction(name: String) : AlreadyExist("DEDUCTION_ALREADY_EXISTS", "Deduction name already exists $name")
        class FinancePrinciple(name: String) : AlreadyExist("FINANCE_PRINCIPLE_ALREADY_EXISTS", "FinancePrinciple already exists $name")
        class Provisionable(name: String) : AlreadyExist("PROVISIONABLE_ALREADY_EXISTS", "Provisionable $name exist deja")
        class AllExternalTransactions(): AlreadyExist("ALL_EXTERNAL_TRANSACTION_ALREADY_EXISTS", "All external transactions already")
        class SavingGoal(name: String): AlreadyExist("SAVING_GOAL_ALREADY_EXISTS", "Saving goal $name exist deja")
        class ScheduleInvoice(name: String): AlreadyExist("SCHEDULE_IN_VOICE_EXISTS", "Schedule in voice $name exist deja")
    }

    sealed class BusinessLogic(code: String, message: String): DomainException(code, message) {
        class SavingGoalsDoNotMatch(message: String = "Saving goals do not match; some saving goal not exists") : BusinessLogic("SAVING_GOALS_DO_NOT_MATCH", message)
        class InvalidReliabilityLevel(value: Int? = null) : BusinessLogic("INVALID_RELIABILITY_LEVEL", "The reliability level must be between 0 and 100.${if (value != null) " Provided: $value" else ""}")
        class TransactionsMustNotBeEmpty(message: String = "Transactions must not be empty") : BusinessLogic("TRANSACTIONS_EMPTY", message)
        class AllNewTransactionsAlreadyAdded(message: String = "All new Transactions were already added.") : BusinessLogic("TRANSACTIONS_ALREADY_ADDED", message)
        class TreatedTransactionAlreadyTreated(message: String = "Treated transaction already treated") : BusinessLogic("TRANSACTION_ALREADY_TREATED", message)
        class Validation(message: String) : BusinessLogic("VALIDATION_ERROR", message)
        class InternalLoanAllPendingMustBeReady(message: String = "You have to complete all pending invoices before take a loan"): BusinessLogic("INTERNAL_LOAN_ALL_MUST_NOT_READ", message)
        class InternalLoanAccountNotAllowForCollateral(message: String = "Account type not allow for loan collateral"): BusinessLogic("INTERNAL_LOAD_BAD_CREDIT", message)
        class InternalLoanBadAccountCredit(message: String = "Loan take a credit card"): BusinessLogic("INTERNAL_LOAD_BAD_CREDIT", message)
        class InternalLoanBadConfidenceScore(confidence: Double, message: String = "Confiance insuffisante (${confidence.roundToInt()}%). Le risque de liquidité est trop élevé. pret refuser"): BusinessLogic("INTERNAL_LOAD_BAD_CREDIT", message)
        class InternalLoanLinkCantBeDelete(message: String = "You can't delete any invoice linked to an internal loan"): BusinessLogic("INTERNAL_LOAD_LINK_DELETE", message)
        class InternalLoanRefundNotValid(amount: Double, loanAmount: Double): BusinessLogic("INTERNAL_LOAD_REFUND_NOT_VALID", "L'argent a freezer  $amount$ doit etre inferieur $loanAmount$")
    }
}
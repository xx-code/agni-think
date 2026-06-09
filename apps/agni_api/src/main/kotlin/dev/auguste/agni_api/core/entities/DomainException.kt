package dev.auguste.agni_api.core.entities

import java.util.UUID

sealed class DomainException(val code: String, message: String): Exception(message) {
    sealed class NotFound(code: String, message: String): DomainException(code, message) {
        class Account(id: UUID) : NotFound("ACCOUNT_NOT_FOUND", "Le compte est introuvable ${id.toString()}")
        class Provisionable(id: String) : NotFound("PROVISIONABLE_NOT_FOUND", "Provisionable not found $id")
        class Patrimony(id: String) : NotFound("PATRIMONY_NOT_FOUND", "Patrimony not found $id")
        class FinanceReport(id: String) : NotFound("FINANCE_REPORT_NOT_FOUND", "FinanceReport not found $id")
        class Currency(id: String) : NotFound("CURRENCY_NOT_FOUND", "Currency not found $id")
        class Notification(id: String) : NotFound("NOTIFICATION_NOT_FOUND", "Notification not found $id")
        class SavingGoal(id: String) : NotFound("SAVING_GOAL_NOT_FOUND", "Saving goal not found $id")
        class IncomeSource(id: String) : NotFound("INCOME_SOURCE_NOT_FOUND", "Income source not found $id")
        class Budget(id: String) : NotFound("BUDGET_NOT_FOUND", "Budget not found $id")
        class Tag(idOrName: String) : NotFound("TAG_NOT_FOUND", "Tag not found $idOrName")
        class ExternalTransaction(id: String) : NotFound("EXTERNAL_TRANSACTION_NOT_FOUND", "External transaction not found $id")
        class Deduction(id: String) : NotFound("DEDUCTION_NOT_FOUND", "Deduction not found $id")
        class Snapshot(id: String) : NotFound("SNAPSHOT_NOT_FOUND", "Snapshot not found $id")
        class BankRegister(id: String) : NotFound("BANK_REGISTER_NOT_FOUND", "Bank Register not found $id")
    }

    sealed class AlreadyExist(code: String, message: String): DomainException(code, message) {
        class Account(name: String) : AlreadyExist("ACCOUNT_ALREADY_EXISTS", "Le compte existe deja $name")
        class Patrimony(name: String) : AlreadyExist("PATRIMONY_ALREADY_EXISTS", "Patrimony name already exists $name")
        class Budget(name: String) : AlreadyExist("BUDGET_ALREADY_EXISTS", "Budget name already exists $name")
        class IncomeSource(name: String) : AlreadyExist("INCOME_SOURCE_ALREADY_EXISTS", "Income source already exists $name")
        class Currency(name: String) : AlreadyExist("CURRENCY_ALREADY_EXISTS", "Currency already exists $name")
        class Tag(name: String) : AlreadyExist("TAG_ALREADY_EXISTS", "Tag with name already exists $name")
        class Deduction(name: String) : AlreadyExist("DEDUCTION_ALREADY_EXISTS", "Deduction name already exists $name")
    }

    sealed class BusinessLogic(code: String, message: String): DomainException(code, message) {
        class NotAllAccountsFound(ids: String? = null) : BusinessLogic("NOT_ALL_ACCOUNTS_FOUND", "Not all accounts are found${if (ids != null) " $ids" else ""}")
        class SavingGoalsDoNotMatch(message: String = "Saving goals do not match; some saving goal not exists") : BusinessLogic("SAVING_GOALS_DO_NOT_MATCH", message)
        class InvalidReliabilityLevel(value: Int? = null) : BusinessLogic("INVALID_RELIABILITY_LEVEL", "The reliability level must be between 0 and 100.${if (value != null) " Provided: $value" else ""}")
        class SomeAccountsNotFound(message: String = "SOME_ACCOUNTS_NOT_FOUND") : BusinessLogic("SOME_ACCOUNTS_NOT_FOUND", message)
        class TransactionsMustNotBeEmpty(message: String = "Transactions must not be empty") : BusinessLogic("TRANSACTIONS_EMPTY", message)
        class AllNewTransactionsAlreadyAdded(message: String = "All new Transactions were already added.") : BusinessLogic("TRANSACTIONS_ALREADY_ADDED", message)
        class TreatedTransactionAlreadyTreated(message: String = "Treated transaction already treated") : BusinessLogic("TRANSACTION_ALREADY_TREATED", message)
    }
}
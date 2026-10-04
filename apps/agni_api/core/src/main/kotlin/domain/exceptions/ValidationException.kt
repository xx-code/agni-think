package domain.exceptions

import domain.enums.ErrorCodeType
import domain.enums.GoalEvaluationType
import java.time.LocalDate

/**
 * Business rules and input validations rejected by the domain.
 *
 * Every leaf owns a dedicated `errorKey` and a `metadata` map so the localized template can
 * interpolate the offending values with `{{placeholder}}`.
 */
sealed class ValidationException(errorKey: String, metadata: Map<String, Any>) : BaseException(
    errorKey, code = ErrorCodeType.BUSINESS_LOGIC, metadata = metadata,
) {
    class BadType(typeName: String, value: String) :
        ValidationException("BAD_TYPE", mapOf("type" to typeName, "value" to value))

    /**
     * Generic escape hatch for a business rule that does not warrant its own leaf yet.
     * Prefer a dedicated leaf so the message can be translated in the catalogs; use this one
     * only as a stopgap, it forwards the developer supplied [reason] to the `VALIDATION_ERROR`
     * template.
     */
    class Validation(reason: String) :
        ValidationException("VALIDATION_ERROR", mapOf("reason" to reason))

    class SavingGoalsDoNotMatch : ValidationException("SAVING_GOALS_DO_NOT_MATCH", mapOf())

    class InvalidReliabilityLevel(value: Int? = null) : ValidationException(
        "INVALID_RELIABILITY_LEVEL",
        value?.let { mapOf("value" to it) } ?: mapOf(),
    )

    class TransactionsMustNotBeEmpty : ValidationException("TRANSACTIONS_EMPTY", mapOf())

    class AllNewTransactionsAlreadyAdded : ValidationException("TRANSACTIONS_ALREADY_ADDED", mapOf())

    class TreatedTransactionAlreadyTreated : ValidationException("TRANSACTION_ALREADY_TREATED", mapOf())

    class InternalLoanAllPendingMustBeReady : ValidationException("INTERNAL_LOAN_ALL_MUST_NOT_READ", mapOf())

    class InternalLoanAccountNotAllowForCollateral :
        ValidationException("INTERNAL_LOAD_BAD_CREDIT", mapOf())

    class InternalLoanBadAccountCredit : ValidationException("INTERNAL_LOAD_BAD_CREDIT", mapOf())

    class InternalLoanBadConfidenceScore(confidence: Double) :
        ValidationException("INTERNAL_LOAD_BAD_CONFIDENCE_SCORE", mapOf("confidence" to confidence))

    class InternalLoanLinkCantBeDelete : ValidationException("INTERNAL_LOAD_LINK_DELETE", mapOf())

    class InternalLoanRefundNotValid(amount: Double, loanAmount: Double) : ValidationException(
        "INTERNAL_LOAD_REFUND_NOT_VALID",
        mapOf("amount" to amount, "loanAmount" to loanAmount),
    )

    class GoalStrategyNotExist(type: GoalEvaluationType) :
        ValidationException("GOAL_STRATEGY_NOT_EXIST", mapOf("type" to type))

    class GoalTargetAmountMustBeLeastFund(balance: Double, targetAmount: Double) : ValidationException(
        "GOAL_TARGET_AMOUNT_MUST_LEAST_FUND",
        mapOf("balance" to balance, "targetAmount" to targetAmount),
    )

    class ProvisionWithLoanMustHaveAScheduleInvoice :
        ValidationException("PROVISION_WITH_LOAN_MUST_HAVE_AS_SCHEDULE_INVOICE", mapOf())

    class ProvisionWithoutLoanMustNotHaveFundAmortization :
        ValidationException("PROVISION_WITHOUT_LOAN_MUST_HAVE_NOT_FUND_AMORTIZATION", mapOf())

    class ProvisionWithLoanMustHaveCantBeByDay :
        ValidationException("PROVISION_WITH_LOAN_CANT_BE_BY_DAY", mapOf())

    class ForcastAdditionalSavingAmountMustLessThanBalance(balance: Double, amount: Double) :
        ValidationException(
            "FORCAST_SAVING_ADDITIONAL_AMOUNT",
            mapOf("balance" to balance, "amount" to amount),
        )

    class CantDeleteSystemCategory(title: String) :
        ValidationException("CANT_DELETE_SYSTEM_CATEGORY", mapOf("title" to title))

    class CantDeleteSystemTag(title: String) :
        ValidationException("CANT_DELETE_SYSTEM_TAG", mapOf("title" to title))

    class CanOnlyCancelTransfer : ValidationException("CAN_ONLY_CANCEL_TRANSFER", mapOf())

    class CantDeleteTransfer : ValidationException("CANT_DELETE_TRANSFER", mapOf())

    class CanNotEditSchedulerInvoiceWithModuleLinkDirectly :
        ValidationException("CANNOT_EDIT_SCHEDULE_INVOICE_WITH_MODULE_LINKER_DIRECTLY", mapOf())

    class YouHaveToSelectOnlyAmortizationFund : ValidationException("SELECT_ONLY_AMORTIZATION_FUND", mapOf())

    class InvalidColor(color: String) : ValidationException("INVALID_COLOR", mapOf("color" to color))

    class ProvisionDepreciateLoanInterestPositif(interest: Double) :
        ValidationException("PROVISION_DEPRECIATE_INTEREST_POSITIF", mapOf("interest" to interest))

    class ProvisionDepreciateLoanMonthMustBeGreaterThanZero(month: Long) : ValidationException(
        "PROVISION_DEPRECIATE_LOAN_MONTH_MUST_BE_GREATER_THAN_ZERO",
        mapOf("month" to month),
    )

    class ProvisionDepreciateCriteriaDecliningBalanceMustHaveRangeGreaterThanZero(montRange: Int) :
        ValidationException(
            "PROVISION_DEPRECIATE_CRITERIA_DECLINING_BALANCE_MUST_HAVE_RANGE_GREATER_THAN_ZERO",
            mapOf("montRange" to montRange),
        )

    class ScheduleFreezeInvoiceMustHaveAScheduler :
        ValidationException("SCHEDULE_FREEZE_INVOICE_SCHEDULER_INVALID", mapOf())

    class ProfileMaxWishlistAmountMustBePositif :
        ValidationException("PROFILE_MAX_WISHLIST_MUST_BE_POSITIF", mapOf())

    class ProfileRulePercentageMustBePositif(rule: String, percentage: Double, total: Double) :
        ValidationException(
            "PROFILE_BUDGET_RULE_PERCENTAGE_ERROR",
            mapOf("rule" to rule, "percentage" to percentage, "total" to total),
        )

    class GetBankRegisterAccessCodeEmpty : ValidationException("GET_BANK_REGISTER_ACCESS_CODE_EMPTY", mapOf())

    class BalanceBufferMustBeGreaterOrEqualToZero(value: Double) :
        ValidationException("BALANCE_BUFFER_MUST_BE_GREATER_OR_EQUAL_ZERO", mapOf("value" to value))

    class ProvisionInitialMustHaveCost : ValidationException("PROVISION_INITIAL_MUST_HAVE_COST", mapOf())

    class AccountBalanceMustBeGreaterThanZero : ValidationException("ACCOUNT_BALANCE_MUST_BE_GREATER_THAN_ZERO", mapOf())

    class FinancePrincipleNameLengthInvalid :
        ValidationException("FINANCE_PRINCIPLE_NAME_LENGTH_INVALID", mapOf())

    class InternalLoanCollateralBalanceNotAllowed :
        ValidationException("INTERNAL_LOAN_COLLATERAL_BALANCE_NOT_ALLOWED", mapOf())

    class InvoiceDeductionCannotBeNegative : ValidationException("INVOICE_DEDUCTION_CANNOT_BE_NEGATIVE", mapOf())

    class SomeExternalTransactionsNotFound :
        ValidationException("SOME_EXTERNAL_TRANSACTIONS_NOT_FOUND", mapOf())

    class IntervalCannotBeZero : ValidationException("INTERVAL_CANNOT_BE_ZERO", mapOf())

    class AmountMustBeNonNegative : ValidationException("AMOUNT_MUST_BE_NON_NEGATIVE", mapOf())

    // -- Fund
    class SavingGoalAmortizationFundMustNotHaveAccount :
        ValidationException("SAVING_GOAL_AMORTIZATION_FUND_MUST_NOT_HAVE_ACCOUNT", mapOf())

    class SavingGoalAmountMustBeGreaterThanZero :
        ValidationException("SAVING_GOAL_AMOUNT_MUST_BE_GREATER_THAN_ZERO", mapOf())

    class SavingGoalBalanceMustBeGreaterThanAmount :
        ValidationException("SAVING_GOAL_BALANCE_MUST_BE_GREATER_THAN_AMOUNT", mapOf())

    class SavingGoalBalanceMustBeLesserThanAmount :
        ValidationException("SAVING_GOAL_BALANCE_MUST_BE_LESSER_THAN_AMOUNT", mapOf())

    class SavingGoalAccountIdMustNotMatch : ValidationException("SAVING_GOAL_ACCOUNT_ID_MUST_NOT_MATCH", mapOf())

    class SavingGoalAccountIdMustNotBeNull :
        ValidationException("SAVING_GOAL_ACCOUNT_ID_MUST_NOT_BE_NULL", mapOf())

    class ScheduleInvoiceAccountNotFound : ValidationException("SCHEDULE_INVOICE_ACCOUNT_NOT_FOUND", mapOf())

    class ScheduleInvoiceCategoryIdMustBeDefined :
        ValidationException("SCHEDULE_INVOICE_CATEGORY_ID_MUST_BE_DEFINED", mapOf())

    class ScheduleInvoiceTypeNotDefined(type: String) :
        ValidationException("SCHEDULE_INVOICE_TYPE_NOT_DEFINED", mapOf("type" to type))

    class ScheduleInvoiceAmountMustBeGreaterThanZero :
        ValidationException("SCHEDULE_INVOICE_AMOUNT_MUST_BE_GREATER_THAN_ZERO", mapOf())

    // --- Agent suggestion ---

    class AgentSuggestionInvalidConfidenceScore(confidence: Double):
        ValidationException("AGENT_SUGGESTION_INVALID_CONFIDENCE_SCORE", mapOf(
            "confidence" to confidence
        ))

    // --- Budget ---

    class InvalidBudgetTarget(targetAmount: Double): ValidationException("INVALID_BUDGET_TARGET", mapOf(
        "target" to targetAmount
    ))

    // --- Finance principle ---

    class InvalidFinancialPrincipleStrictness(strictness: Int) :
        ValidationException("INVALID_FINANCIAL_PRINCIPLE_STRICTNESS", mapOf(
            "strictness" to strictness
        ))

    // --- Fund ---

    class FundTargetAmountMustGreaterThanZero(target: Double) :
        ValidationException("FUND_TARGET_AMOUNT_MUST_BE_GREATER_THAN_ZERO", mapOf("target" to target))

    // --- Goal ---

    class GoalTargetAmountMustBeGreaterThanZero(targetAmount: Double) :
        ValidationException("GOAL_TARGET_AMOUNT_MUST_BE_GREATER_THAN_ZERO", mapOf("targetAmount" to targetAmount))

    // --- Income source ---

    class IncomeSourceReliabilityLevelInvalid(reliabilityLevel: Int) : ValidationException(
        "INCOME_SOURCE_RELIABILITY_LEVEL_INVALID", mapOf("reliabilityLevel" to reliabilityLevel),
    )

    class IncomeSourceTaxRateInvalid(taxRate: Double) :
        ValidationException("INCOME_SOURCE_TAX_RATE_INVALID", mapOf("taxRate" to taxRate))

    class IncomeSourceOtherRateInvalid(rate: Double) :
        ValidationException("INCOME_SOURCE_OTHER_RATE_INVALID", mapOf("rate" to rate))

    class IncomeSourceAnnualGrossAmountMustBePositif(annualGrossAmount: Double?) :
        ValidationException("INCOME_SOURCE_ANNUAL_GROSS_AMOUNT_MUST_BE_POSITIF", mapOf("annualGrossAmount" to annualGrossAmount.toString()))

    class IncomeSourceStartDateMustLessThanEndDate(startDate: LocalDate, endDate: LocalDate?) : ValidationException(
        "INCOME_SOURCE_START_DATE_MUST_BE_LESS_THAN_END_DATE",
        mapOf("startDate" to startDate, "endDate" to (endDate ?: "")),
    )

    class IncomeSourceEndDateMustGreaterThanStartDate(startDate: LocalDate, endDate: LocalDate?) : ValidationException(
        "INCOME_SOURCE_END_DATE_MUST_BE_GREATER_THAN_START_DATE",
        mapOf("startDate" to startDate, "endDate" to (endDate ?: "")),
    )

    // --- Scheduled invoice ---

    class SchedulerInvoiceAmountShouldGreaterThanZero(amount: Double) :
        ValidationException("SCHEDULE_INVOICE_AMOUNT_SHOULD_BE_GREATER_THAN_ZERO", mapOf("amount" to amount))

    // --- Spending period ---

    class SpendingPeriodStartDateMustBeLesserThanEndDate(startDate: LocalDate, endDate: LocalDate) : ValidationException(
        "SPENDING_PERIOD_START_DATE_MUST_BE_LESS_THAN_END_DATE",
        mapOf("startDate" to startDate, "endDate" to endDate),
    )

    class SpendingPeriodEndDateMustBeGreaterThanStartDate(startDate: LocalDate, endDate: LocalDate) : ValidationException(
        "SPENDING_PERIOD_END_DATE_MUST_BE_GREATER_THAN_START_DATE",
        mapOf("startDate" to startDate, "endDate" to endDate),
    )

    // --- Spending period template ---

    class SpendingPeriodTemplateStartDateMustBeLesserThanEndDate(startDate: LocalDate, endDate: LocalDate?):
        ValidationException("SPENDING_PERIOD_TEMPLATE_START_DATE_MUST_BE_LESS_THAN_END_DATE",
            mapOf("startDate" to startDate, "endDate" to endDate.toString()),
    )

    class SpendingPeriodTemplateEndDateMustBeGreaterThanStartDate(startDate: LocalDate, endDate: LocalDate?):
        ValidationException("SPENDING_PERIOD_TEMPLATE_END_DATE_MUST_BE_GREATER_THAN_START_DATE",
            mapOf("startDate" to startDate, "endDate" to endDate.toString()),
    )

    // --- Transaction ---

    class TransactionAmountMustBeGreaterThanZero(amount: Double) :
        ValidationException("TRANSACTION_AMOUNT_MUST_BE_GREATER_THAN_ZERO", mapOf("amount" to amount))
}

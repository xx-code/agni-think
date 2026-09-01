package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.usecases.analystics.dto.WantItemOutput
import jakarta.validation.constraints.Min
import org.hibernate.validator.constraints.Range
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class ApiGetBudgetingRuleModel(
    val period: String?,
    val interval: Int = 0,

    @field:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val startDate: LocalDateTime?,
    @field:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val endDate: LocalDateTime?
)

data class ApiGetSavingAnalyticModel(
    val period: String,
    val interval: Int,

    @field:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val startDate: LocalDateTime = LocalDateTime.now()
)

data class ApiGetCategoryAnalyticModel(
    val period: String,
    val interval: Int,

    @field:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val startDate: LocalDateTime = LocalDateTime.now(),

    val offset: Int = 0,
    val limit: Int = 10,
    val queryAll: Boolean = false

    )

data class ApiGetTagAnalyticModel(
    val period: String,
    val interval: Int,

    @field:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val startDate: LocalDateTime = LocalDateTime.now(),

    val offset: Int = 0,
    val limit: Int = 10,
    val queryAll: Boolean = false,
    val categoryId: UUID? = null
)

data class ApiGetPatrimonyEvolutionModel(
    val period: String,
    val interval: Int
)


data class ApiSavingAdditionalIncomeModel(
    val savingAccountId: UUID,
    val amount: Double
)
data class ApiWantItemModel(
    val description: String,
    @field:Min(value = 0, message = "Amount Want item must be greater than 0")
    val amount: Double,
)

data class ApiForcastSpendingModel(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val wantItems: List<ApiWantItemModel>,
    val savingAdditionalIncome: List<ApiSavingAdditionalIncomeModel>,
    val budgetIds: List<UUID>,
    val overrideAccountsBalance: Double? = null,
    @field:Range(min = 0, max = 100, message = "Le taux d'épargne doit être compris entre 0 et 100")
    val savingRate: Double? = null,
)
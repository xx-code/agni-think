package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiGetCategoryAnalyticModel
import dev.auguste.agni_api.controllers.models.ApiGetSavingAnalyticModel
import dev.auguste.agni_api.controllers.models.ApiGetTagAnalyticModel
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.entities.enums.PeriodType
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetFinanceProfileOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSavingAnalyticInput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSavingAnalyticOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSpendByCategoryInput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSpendByCategoryOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSpendByTagInput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSpendByTagOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v2/analytics")
class AnalyticController(
    private val getSpendTagAnalytic: IUseCase<GetSpendByTagInput, ListOutput<GetSpendByTagOutput>>,
    private val getSpendCategoryAnalytic: IUseCase<GetSpendByCategoryInput, ListOutput<GetSpendByCategoryOutput>>,
    private val getSavingAnalytic: IUseCase<GetSavingAnalyticInput, GetSavingAnalyticOutput>,
    private val getFinanceProfile: IUseCase<Unit, GetFinanceProfileOutput>
) {
    @GetMapping("/spend-categories")
    fun getSpendCategoriesAnalytic(query: ApiGetCategoryAnalyticModel) : ResponseEntity<ListOutput<GetSpendByCategoryOutput>> {
        return ResponseEntity.ok(getSpendCategoryAnalytic.execAsync(
            GetSpendByCategoryInput(
                period = PeriodType.fromString(query.period),
                interval = query.interval,
                startDate = query.startDate,
                query = QueryFilter(
                    query.offset,
                    query.limit,
                    query.queryAll
                )
            )
        ))
    }

    @GetMapping("/spend-tags")
    fun getSpendTagsAnalytic(query: ApiGetTagAnalyticModel) : ResponseEntity<ListOutput<GetSpendByTagOutput>> {
        return ResponseEntity.ok(getSpendTagAnalytic.execAsync(
            GetSpendByTagInput(
                period = PeriodType.fromString(query.period),
                interval = query.interval,
                startDate = query.startDate,
                query = QueryFilter(
                    query.offset,
                    query.limit,
                    query.queryAll
                ),
                categoryId = query.categoryId
            )
        ))
    }

    @GetMapping("/savings")
    fun getSavingAnalytic(query: ApiGetSavingAnalyticModel) : ResponseEntity<GetSavingAnalyticOutput> {
        return ResponseEntity.ok(getSavingAnalytic.execAsync(
            GetSavingAnalyticInput(
                period = PeriodType.fromString(query.period),
                interval = query.interval,
                startDate = query.startDate
            )
        ))
    }

    @GetMapping("/finance-profile")
    fun getFinanceProfile() : ResponseEntity<GetFinanceProfileOutput> {
        return ResponseEntity.ok(getFinanceProfile.execAsync(Unit))
    }
}
package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiGetAnalysticModel
import dev.auguste.agni_api.core.entities.enums.PeriodType
import dev.auguste.agni_api.core.usecases.analystics.dto.GetAnalysticInput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSavingAnalyticOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSpendAnalyticOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v2/analytics")
class AnalyticController(
    private val getSpendAnalytic: IUseCase<GetAnalysticInput, GetSpendAnalyticOutput>,
    private val getSavingAnalytic: IUseCase<GetAnalysticInput, GetSavingAnalyticOutput>
) {
    @GetMapping("/spend")
    fun getSpendAnalytic(query: ApiGetAnalysticModel) : ResponseEntity<GetSpendAnalyticOutput> {
        return ResponseEntity.ok(getSpendAnalytic.execAsync(
            GetAnalysticInput(
                period = PeriodType.fromString(query.period),
                interval = query.interval,
                startDate = query.startDate
            )
        ))
    }

    @GetMapping("/savings")
    fun getSavingAnalytic(query: ApiGetAnalysticModel) : ResponseEntity<GetSavingAnalyticOutput> {
        return ResponseEntity.ok(getSavingAnalytic.execAsync(
            GetAnalysticInput(
                period = PeriodType.fromString(query.period),
                interval = query.interval,
                startDate = query.startDate
            )
        ))
    }
}
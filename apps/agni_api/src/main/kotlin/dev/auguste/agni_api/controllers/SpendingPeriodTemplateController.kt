package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiCreateSpendingPeriodTemplateModel
import dev.auguste.agni_api.controllers.models.ApiUpdateSpendingPeriodTemplateModel
import dev.auguste.agni_api.controllers.models.mapApiCreateSpendingPeriodTemplateToSpendingPeriodTemplate
import dev.auguste.agni_api.controllers.models.mapApiUpdateSpendingPeriodTemplateToSpendingPeriodTemplate
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.spending_period_template.dto.CreateSpendingPeriodTemplateInput
import dev.auguste.agni_api.core.usecases.spending_period_template.dto.GetSpendingPeriodTemplateOutput
import dev.auguste.agni_api.core.usecases.spending_period_template.dto.UpdateSpendingPeriodTemplateInput
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v2/spending-period-templates")
class SpendingPeriodTemplateController(
    val createSpendingPeriodTemplateUc: IUseCase<CreateSpendingPeriodTemplateInput, CreatedOutput>,
    val updateSpendingPeriodTemplateUc: IUseCase<UpdateSpendingPeriodTemplateInput, Unit>,
    val getSpendingPeriodTemplateUc: IUseCase<UUID, GetSpendingPeriodTemplateOutput>,
    val getAllSpendingPeriodTemplateUc: IUseCase<QueryFilter, ListOutput<GetSpendingPeriodTemplateOutput>>,
    @Qualifier("deleteSpendingPeriodTemplate") val deleteSpendingPeriodTemplateUc: IUseCase<UUID, Unit>
) {

    @PostMapping
    fun createSpendingPeriodTemplate(@Valid @RequestBody request: ApiCreateSpendingPeriodTemplateModel) : ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createSpendingPeriodTemplateUc.execAsync(
            mapApiCreateSpendingPeriodTemplateToSpendingPeriodTemplate(request)
        ))
    }

    @PutMapping("/{id}")
    fun updateSpendingPeriodTemplate(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpdateSpendingPeriodTemplateModel) : ResponseEntity<Unit> {
        return ResponseEntity.ok(updateSpendingPeriodTemplateUc.execAsync(
            mapApiUpdateSpendingPeriodTemplateToSpendingPeriodTemplate(id, request)
        ))
    }

    @DeleteMapping("/{id}")
    fun deleteSpendingPeriodTemplate(@PathVariable id: UUID) : ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteSpendingPeriodTemplateUc.execAsync(id))
    }

    @GetMapping("/{id}")
    fun getSpendingPeriodTemplate(@PathVariable id: UUID) : ResponseEntity<GetSpendingPeriodTemplateOutput> {
        return ResponseEntity.ok(getSpendingPeriodTemplateUc.execAsync(
            input = id
        ))
    }

    @GetMapping
    fun getAllSpendingPeriodTemplates(query: QueryFilter) : ResponseEntity<ListOutput<GetSpendingPeriodTemplateOutput>> {
        return ResponseEntity.ok(getAllSpendingPeriodTemplateUc.execAsync(
            query
        ))
    }
}
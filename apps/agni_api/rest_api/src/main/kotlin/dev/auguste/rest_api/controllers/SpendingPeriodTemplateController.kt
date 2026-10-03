package dev.auguste.rest_api.controllers

import adapters.dto.QueryFilter
import dev.auguste.rest_api.controllers.models.ApiCreateSpendingPeriodTemplateModel
import dev.auguste.rest_api.controllers.models.ApiUpdateSpendingPeriodTemplateModel
import dev.auguste.rest_api.controllers.models.mapApiCreateSpendingPeriodTemplateToSpendingPeriodTemplate
import dev.auguste.rest_api.controllers.models.mapApiUpdateSpendingPeriodTemplateToSpendingPeriodTemplate
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.spending_period_template.dto.CreateSpendingPeriodTemplateInput
import usecases.spending_period_template.dto.GetSpendingPeriodTemplateOutput
import usecases.spending_period_template.dto.UpdateSpendingPeriodTemplateInput
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
    suspend fun createSpendingPeriodTemplate(@Valid @RequestBody request: ApiCreateSpendingPeriodTemplateModel) : ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createSpendingPeriodTemplateUc.execute(
            mapApiCreateSpendingPeriodTemplateToSpendingPeriodTemplate(request)
        ).getOrThrow())
    }

    @PutMapping("/{id}")
    suspend fun updateSpendingPeriodTemplate(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpdateSpendingPeriodTemplateModel) : ResponseEntity<Unit> {
        return ResponseEntity.ok(updateSpendingPeriodTemplateUc.execute(
            mapApiUpdateSpendingPeriodTemplateToSpendingPeriodTemplate(id, request)
        ).getOrThrow())
    }

    @DeleteMapping("/{id}")
    suspend fun deleteSpendingPeriodTemplate(@PathVariable id: UUID) : ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteSpendingPeriodTemplateUc.execute(id).getOrThrow())
    }

    @GetMapping("/{id}")
    suspend fun getSpendingPeriodTemplate(@PathVariable id: UUID) : ResponseEntity<GetSpendingPeriodTemplateOutput> {
        return ResponseEntity.ok(getSpendingPeriodTemplateUc.execute(
            input = id
        ).getOrThrow())
    }

    @GetMapping
    suspend fun getAllSpendingPeriodTemplates(query: QueryFilter) : ResponseEntity<ListOutput<GetSpendingPeriodTemplateOutput>> {
        return ResponseEntity.ok(getAllSpendingPeriodTemplateUc.execute(
            query
        ).getOrThrow())
    }
}
package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiCreateSpendingPeriodModel
import dev.auguste.agni_api.controllers.models.ApiUpdateSpendingPeriodModel
import dev.auguste.agni_api.controllers.models.mapApiCreateSpendingPeriodToSpendingPeriod
import dev.auguste.agni_api.controllers.models.mapApiUpdateSpendingPeriodToSpendingPeriod
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.spending_period.dto.CreateSpendingPeriodInput
import dev.auguste.agni_api.core.usecases.spending_period.dto.GetAllSpendingPeriodInput
import dev.auguste.agni_api.core.usecases.spending_period.dto.GetSpendingPeriodOutput
import dev.auguste.agni_api.core.usecases.spending_period.dto.UpdateSpendingPeriodInput
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
@RequestMapping("/v2/spending-periods")
class SpendingPeriodController(
    val createSpendingPeriodUc: IUseCase<CreateSpendingPeriodInput, CreatedOutput>,
    val updateSpendingPeriodUc: IUseCase<UpdateSpendingPeriodInput, Unit>,
    val getSpendingPeriodUc: IUseCase<UUID, GetSpendingPeriodOutput>,
    val getAllSpendingPeriodUc: IUseCase<GetAllSpendingPeriodInput, ListOutput<GetSpendingPeriodOutput>>,
    @Qualifier("deleteSpendingPeriod") val deleteSpendingPeriodUc: IUseCase<UUID, Unit>
) {

    @PostMapping
    fun createSpendingPeriod(@Valid @RequestBody request: ApiCreateSpendingPeriodModel): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createSpendingPeriodUc.execAsync(
            mapApiCreateSpendingPeriodToSpendingPeriod(request)
        ))
    }

    @PutMapping("/{id}")
    fun updateSpendingPeriod(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpdateSpendingPeriodModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateSpendingPeriodUc.execAsync(
            mapApiUpdateSpendingPeriodToSpendingPeriod(id, request)
        ))
    }

    @DeleteMapping("/{id}")
    fun deleteSpendingPeriod(@PathVariable id: UUID): ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteSpendingPeriodUc.execAsync(id))
    }

    @GetMapping("/{id}")
    fun getSpendingPeriod(@PathVariable id: UUID): ResponseEntity<GetSpendingPeriodOutput> {
        return ResponseEntity.ok(getSpendingPeriodUc.execAsync(
            input = id
        ))
    }

    @GetMapping
    fun getAllSpendingPeriods(query: GetAllSpendingPeriodInput): ResponseEntity<ListOutput<GetSpendingPeriodOutput>> {
        return ResponseEntity.ok(getAllSpendingPeriodUc.execAsync(
            query
        ))
    }
}

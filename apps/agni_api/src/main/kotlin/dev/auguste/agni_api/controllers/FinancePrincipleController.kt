package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiCreateFinancePrincipleModel
import dev.auguste.agni_api.controllers.models.ApiUpdateFinancePrincipleModel
import dev.auguste.agni_api.controllers.models.mapApiCreateFinancePrincipleTo
import dev.auguste.agni_api.controllers.models.mapApiUpdateFinancePrincipleTo
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.finance_principles.dto.CreateFinancePrincipleInput
import dev.auguste.agni_api.core.usecases.finance_principles.dto.DeleteFinancePrincipleInput
import dev.auguste.agni_api.core.usecases.finance_principles.dto.GetFinancePrincipleOutput
import dev.auguste.agni_api.core.usecases.finance_principles.dto.UpdateFinancePrincipleInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
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
@RequestMapping("/v2/finance-principles")
class FinancePrincipleController (
    val createFinancePrinciple: IUseCase<CreateFinancePrincipleInput, CreatedOutput>,
    val updateFinancePrinciple: IUseCase<UpdateFinancePrincipleInput, Unit>,
    val deleteFinancePrinciple: IUseCase<DeleteFinancePrincipleInput, Unit>,
    val getFinancePrinciple: IUseCase<UUID, GetFinancePrincipleOutput>,
    val getAllFinancePrinciple: IUseCase<QueryFilter, ListOutput<GetFinancePrincipleOutput>>
){

    @PostMapping
    fun createFinancePrinciple(@RequestBody request: ApiCreateFinancePrincipleModel): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createFinancePrinciple.execAsync(mapApiCreateFinancePrincipleTo(request)))
    }

    @PutMapping("/{id}")
    fun updateFinancePrinciple(@PathVariable id: UUID, @RequestBody request: ApiUpdateFinancePrincipleModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateFinancePrinciple.execAsync(mapApiUpdateFinancePrincipleTo(id, request)))
    }

    @DeleteMapping("/{id}")
    fun deleteFinancePrinciple(@PathVariable id: UUID): ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteFinancePrinciple.execAsync(DeleteFinancePrincipleInput(id)))
    }

    @GetMapping("/{id}")
    fun getFinancePrinciple(@PathVariable id: UUID): ResponseEntity<GetFinancePrincipleOutput> {
        return ResponseEntity.ok(getFinancePrinciple.execAsync(id))
    }

    @GetMapping
    fun getAllFinancePrinciple(query: QueryFilter): ResponseEntity<ListOutput<GetFinancePrincipleOutput>> {
        return ResponseEntity.ok(getAllFinancePrinciple.execAsync(query))
    }
}
package dev.auguste.rest_api.controllers

import adapters.dto.QueryFilter
import dev.auguste.rest_api.controllers.models.ApiCreateFinancePrincipleModel
import dev.auguste.rest_api.controllers.models.ApiUpdateFinancePrincipleModel
import dev.auguste.rest_api.controllers.models.mapApiCreateFinancePrincipleTo
import dev.auguste.rest_api.controllers.models.mapApiUpdateFinancePrincipleTo
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.finance_principles.dto.CreateFinancePrincipleInput
import usecases.finance_principles.dto.DeleteFinancePrincipleInput
import usecases.finance_principles.dto.GetFinancePrincipleOutput
import usecases.finance_principles.dto.UpdateFinancePrincipleInput
import usecases.interfaces.IUseCase
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
    suspend fun createFinancePrinciple(@RequestBody request: ApiCreateFinancePrincipleModel): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createFinancePrinciple.execute(mapApiCreateFinancePrincipleTo(request)).getOrThrow())
    }

    @PutMapping("/{id}")
    suspend fun updateFinancePrinciple(@PathVariable id: UUID, @RequestBody request: ApiUpdateFinancePrincipleModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateFinancePrinciple.execute(mapApiUpdateFinancePrincipleTo(id, request)).getOrThrow())
    }

    @DeleteMapping("/{id}")
    suspend fun deleteFinancePrinciple(@PathVariable id: UUID): ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteFinancePrinciple.execute(DeleteFinancePrincipleInput(id)).getOrThrow())
    }

    @GetMapping("/{id}")
    suspend fun getFinancePrinciple(@PathVariable id: UUID): ResponseEntity<GetFinancePrincipleOutput> {
        return ResponseEntity.ok(getFinancePrinciple.execute(id).getOrThrow())
    }

    @GetMapping
    suspend fun getAllFinancePrinciple(query: QueryFilter): ResponseEntity<ListOutput<GetFinancePrincipleOutput>> {
        return ResponseEntity.ok(getAllFinancePrinciple.execute(query).getOrThrow())
    }
}
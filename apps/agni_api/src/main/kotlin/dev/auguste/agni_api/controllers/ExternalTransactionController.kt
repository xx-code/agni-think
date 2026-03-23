package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiAppExternalTransactionModel
import dev.auguste.agni_api.controllers.models.mapApiExternalTransactionModel
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.AddExternalTransactionInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetAllExternalTransactionInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetExternalTransactionOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.TreatAnExternalTransactionInput
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v2/external-transactions")
class ExternalTransactionController(
    private val addExternalTransaction: IUseCase<AddExternalTransactionInput, CreatedOutput>,
    private val addManyExternalTransaction: IUseCase<List<AddExternalTransactionInput>, List<CreatedOutput>>,
    private val getAllExternalTransaction: IUseCase<GetAllExternalTransactionInput, ListOutput<GetExternalTransactionOutput>>,
    private val treatAnExternalTransaction: IUseCase<TreatAnExternalTransactionInput, Unit>,
) {
    @PostMapping
    fun addExternalTransaction(@RequestBody input: ApiAppExternalTransactionModel): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(addExternalTransaction.execAsync(mapApiExternalTransactionModel(input)))
    }

    @PostMapping("/many")
    fun addManyExternalTransaction(@RequestBody input: List<ApiAppExternalTransactionModel>): ResponseEntity<List<CreatedOutput>> {
        val request = input.map {mapApiExternalTransactionModel((it))}
        return ResponseEntity.ok(addManyExternalTransaction.execAsync(request))
    }

    @GetMapping
    fun getAllExternalTransaction(query: QueryFilter, isTreated: Boolean?=null): ResponseEntity<ListOutput<GetExternalTransactionOutput>> {
        return ResponseEntity.ok(getAllExternalTransaction.execAsync(GetAllExternalTransactionInput(query, isTreated)))
    }

    @PostMapping("/treat/{id}")
    fun treatAnExternalTransaction(@PathVariable id: UUID): ResponseEntity<Unit> {
        return ResponseEntity.ok(treatAnExternalTransaction.execAsync(TreatAnExternalTransactionInput(
            transactionId = id
        )))
    }
}
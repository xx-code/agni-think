package dev.auguste.rest_api.controllers

import adapters.dto.QueryFilter
import dev.auguste.rest_api.controllers.models.ApiAppExternalTransactionModel
import dev.auguste.rest_api.controllers.models.mapApiExternalTransactionModel
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.AddExternalTransactionInput
import usecases.invoices.dto.GetAllExternalTransactionInput
import usecases.invoices.dto.GetExternalTransactionOutput
import usecases.invoices.dto.TreatAnExternalTransactionInput
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
    suspend fun addExternalTransaction(@RequestBody input: ApiAppExternalTransactionModel): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(addExternalTransaction.execute(mapApiExternalTransactionModel(input)).getOrThrow())
    }

    @PostMapping("/many")
    suspend fun addManyExternalTransaction(@RequestBody input: List<ApiAppExternalTransactionModel>): ResponseEntity<List<CreatedOutput>> {
        val request = input.map { mapApiExternalTransactionModel((it)) }
        return ResponseEntity.ok(addManyExternalTransaction.execute(request).getOrThrow())
    }

    @GetMapping
    suspend fun getAllExternalTransaction(query: QueryFilter, isTreated: Boolean?=null): ResponseEntity<ListOutput<GetExternalTransactionOutput>> {
        return ResponseEntity.ok(getAllExternalTransaction.execute(GetAllExternalTransactionInput(query, isTreated)).getOrThrow())
    }

    @PostMapping("/treat/{id}")
    suspend fun treatAnExternalTransaction(@PathVariable id: UUID): ResponseEntity<Unit> {
        return ResponseEntity.ok(treatAnExternalTransaction.execute(TreatAnExternalTransactionInput(
            transactionId = id
        )).getOrThrow())
    }
}
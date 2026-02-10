package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiCreateFreezeInvoiceModel
import dev.auguste.agni_api.controllers.models.ApiCreateInvoiceModel
import dev.auguste.agni_api.controllers.models.ApiQueryInvoice
import dev.auguste.agni_api.controllers.models.ApiTransferInvoiceModel
import dev.auguste.agni_api.controllers.models.ApiUpdateInvoiceModel
import dev.auguste.agni_api.controllers.models.mapApiCreateFreezeInvoice
import dev.auguste.agni_api.controllers.models.mapApiCreateInvoice
import dev.auguste.agni_api.controllers.models.mapApiTransfer
import dev.auguste.agni_api.controllers.models.mapApiUpdateInvoice
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IInnerUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.CompleteInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateFreezeInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.DeleteInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetAllInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalancesByPeriodInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetInvoiceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.TransferInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.UpdateInvoiceInput
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v2/invoices")
class InvoiceController(
    private val createInvoiceUseCase: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
    private val getInvoiceUseCase: IUseCase<UUID, GetInvoiceOutput>,
    private val getAllInvoiceUseCase: IUseCase<GetAllInvoiceInput, ListOutput<GetInvoiceOutput>>,
    private val updateInvoiceUseCase: IUseCase<UpdateInvoiceInput, Unit>,
    private val deleteInvoiceUseCase: IInnerUseCase<DeleteInvoiceInput, Unit>,
    private val transferInvoiceUseCase: IUseCase<TransferInvoiceInput, Unit>,
    private val getBalanceUseCase: IUseCase<GetBalanceInput, GetBalanceOutput>,
    private val getBalanceByPeriodUseCase: IUseCase<GetBalancesByPeriodInput, List<GetBalanceOutput>>,
    private val createFreezeInvoiceUseCase: IUseCase<CreateFreezeInvoiceInput, CreatedOutput>,
    private val completeInvoiceUseCase: IUseCase<CompleteInvoiceInput, Unit>,
) {

    @PostMapping
    fun createInvoice(@RequestBody request: ApiCreateInvoiceModel) : ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createInvoiceUseCase.execAsync(
            mapApiCreateInvoice(request)
        ))
    }

    @PutMapping("/{id}")
    fun updateInvoice(@PathVariable id: UUID, @RequestBody request: ApiUpdateInvoiceModel) : ResponseEntity<Unit> {
        return ResponseEntity.ok(updateInvoiceUseCase.execAsync(
            mapApiUpdateInvoice(id, request)
        ))
    }

    @DeleteMapping("/{id}")
    fun deleteInvoice(@PathVariable id: UUID) : ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteInvoiceUseCase.execAsync(
            DeleteInvoiceInput(id)
        ))
    }

    @GetMapping("/{id}")
    fun getInvoice(@PathVariable id: UUID) : ResponseEntity<GetInvoiceOutput> {
        return ResponseEntity.ok(getInvoiceUseCase.execAsync(
            id
        ))
    }

    @GetMapping
    fun getAllInvoices(query: QueryFilter, extend: ApiQueryInvoice) : ResponseEntity<ListOutput<GetInvoiceOutput>> {
        return ResponseEntity.ok(getAllInvoiceUseCase.execAsync(
            GetAllInvoiceInput(
                query,
                accountIds = extend.accountIds,
                startDate = extend.startDate,
                endDate = extend.endDate,
                status = extend.status,
                types = extend.types,
                isFreeze = extend.isFreeze,
                mouvementType = extend.mouvement,
                categoryIds = extend.categoryIds,
                tagIds = extend.tagIds,
                budgetIds = extend.budgetIds,
                minAmount = extend.minAmount,
                maxAmount = extend.maxAmount
            )
        ))
    }

    @PutMapping("/{id}/completed")
    fun completeInvoice(@PathVariable id: UUID) : ResponseEntity<Unit> {
        return ResponseEntity.ok(completeInvoiceUseCase.execAsync(
            CompleteInvoiceInput(id)
        ))
    }

    @GetMapping("/balances")
    fun getBalance(query: GetBalanceInput) : ResponseEntity<GetBalanceOutput> {
        return ResponseEntity.ok(getBalanceUseCase.execAsync(
            query
        ))
    }

    @GetMapping("/balances-by-period")
    fun getBalancesByPeriod(query: GetBalancesByPeriodInput) : ResponseEntity<List<GetBalanceOutput>> {
        return ResponseEntity.ok(getBalanceByPeriodUseCase.execAsync(
            query
        ))
    }

    @PostMapping("create-freeze")
    fun createFreezeInvoice(@RequestBody request: ApiCreateFreezeInvoiceModel) : ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createFreezeInvoiceUseCase.execAsync(
            mapApiCreateFreezeInvoice(request)
        ))
    }

    @PostMapping("transfer")
    fun transferInvoice(@RequestBody request: ApiTransferInvoiceModel) : ResponseEntity<Unit> {
        return ResponseEntity.ok(transferInvoiceUseCase.execAsync(
            mapApiTransfer(request)
        ))
    }
}
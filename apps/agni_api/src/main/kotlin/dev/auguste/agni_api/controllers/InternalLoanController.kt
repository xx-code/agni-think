package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiCreateInternalLoanModel
import dev.auguste.agni_api.controllers.models.ApiUpdateInternalLoanModel
import dev.auguste.agni_api.controllers.models.mapApiCreateInternalLoanModel
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.internal_loan.dto.CreateInternalLoanInput
import dev.auguste.agni_api.core.usecases.internal_loan.dto.GetInternalLoanOutput
import dev.auguste.agni_api.core.usecases.internal_loan.dto.UpdateInternalLoanInput
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
@RequestMapping("/v2/internal-loans")
class InternalLoanController(
    private val createInternalLoan: IUseCase<CreateInternalLoanInput, CreatedOutput>,
    private val updateInternalLoan: IUseCase<UpdateInternalLoanInput, Unit>,
    @Qualifier("deleteInternalLoan")
    private val deleteInternalLoan: IUseCase<UUID, Unit>,
    private val getInternalLoan: IUseCase<UUID, GetInternalLoanOutput>,
    private val getAllInternalLoan: IUseCase<QueryFilter, ListOutput<GetInternalLoanOutput>>
) {
    @GetMapping("{id}")
    fun getInternalLoan(@PathVariable id: UUID): ResponseEntity<GetInternalLoanOutput> {
        return ResponseEntity.ok(getInternalLoan.execAsync(id))
    }

    @GetMapping
    fun getAllInternalLoans(query: QueryFilter): ResponseEntity<ListOutput<GetInternalLoanOutput>>  {
        return ResponseEntity.ok(getAllInternalLoan.execAsync(query))
    }

    @PostMapping
    fun createInternalLoan(@RequestBody request: ApiCreateInternalLoanModel): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createInternalLoan.execAsync(mapApiCreateInternalLoanModel(request)))
    }

    @PutMapping("{id}")
    fun updateInternalLoan(@PathVariable id: UUID, @RequestBody request: ApiUpdateInternalLoanModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateInternalLoan.execAsync(UpdateInternalLoanInput(id, request.fundAccountId, request.dueDate)))
    }

    @DeleteMapping("{id}")
    fun deleteInternalLoan(@PathVariable id: UUID): ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteInternalLoan.execAsync(id))
    }
}
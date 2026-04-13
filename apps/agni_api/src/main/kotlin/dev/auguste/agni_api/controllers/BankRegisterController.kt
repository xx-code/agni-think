package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiCreateBankRegisterModel
import dev.auguste.agni_api.controllers.models.ApiSecureBankRegisterOutput
import dev.auguste.agni_api.controllers.models.ApiUpdateBankRegisterModel
import dev.auguste.agni_api.controllers.models.mapApiCreateBankRegister
import dev.auguste.agni_api.controllers.models.mapApiUpdateBankRegister
import dev.auguste.agni_api.controllers.models.mapBankRegisterToSecure
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.bank_registers.dto.CreateBankRegisterInput
import dev.auguste.agni_api.core.usecases.bank_registers.dto.GetBankRegisterOutput
import dev.auguste.agni_api.core.usecases.bank_registers.dto.UpdateBankRegisterInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v2/bank-registers")
class BankRegisterController(
    private val createBankRegister: IUseCase<CreateBankRegisterInput, CreatedOutput>,
    private val updateBankRegister: IUseCase<UpdateBankRegisterInput, Unit>,
    private val getAllBankRegisters: IUseCase<QueryFilter, ListOutput<GetBankRegisterOutput>>) {
    @PostMapping
    fun createBankRegister(@RequestBody input: ApiCreateBankRegisterModel): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(
            createBankRegister.execAsync(mapApiCreateBankRegister(input))
        )
    }

    @PutMapping("/{id}")
    fun updateBankRegister(@PathVariable id: UUID, @RequestBody input: ApiUpdateBankRegisterModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(
            updateBankRegister.execAsync(mapApiUpdateBankRegister(id,input))
        )
    }

    @GetMapping("/agent-level")
    fun getAllBankRegisterAgentLevel(query: QueryFilter): ResponseEntity<ListOutput<GetBankRegisterOutput>> {
        return ResponseEntity.ok(
            getAllBankRegisters.execAsync(query)
        )
    }

    @GetMapping
    fun getAllBankRegister(query: QueryFilter): ResponseEntity<ListOutput<ApiSecureBankRegisterOutput>> {
        val result = getAllBankRegisters.execAsync(query)
        return ResponseEntity.ok(
            ListOutput(
                items= result.items.map { mapBankRegisterToSecure(it) },
                total=result.total
            )
        )
    }
}
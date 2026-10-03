package dev.auguste.rest_api.controllers

import adapters.dto.QueryFilter
import dev.auguste.rest_api.controllers.models.ApiCreateBankRegisterModel
import dev.auguste.rest_api.controllers.models.ApiSecureBankRegisterOutput
import dev.auguste.rest_api.controllers.models.ApiUpdateBankRegisterModel
import dev.auguste.rest_api.controllers.models.mapApiCreateBankRegister
import dev.auguste.rest_api.controllers.models.mapApiUpdateBankRegister
import dev.auguste.rest_api.controllers.models.mapBankRegisterToSecure
import usecases.CreatedOutput
import usecases.ListOutput
import usecases.bank_registers.dto.CreateBankRegisterInput
import usecases.bank_registers.dto.GetBankRegisterByAccessCodeInput
import usecases.bank_registers.dto.GetBankRegisterOutput
import usecases.bank_registers.dto.UpdateBankRegisterInput
import usecases.interfaces.IUseCase
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
    private val getAllBankRegisters: IUseCase<QueryFilter, ListOutput<GetBankRegisterOutput>>,
    private val getBankRegisterByAccess: IUseCase<GetBankRegisterByAccessCodeInput, GetBankRegisterOutput>
    ) {
    @PostMapping
    fun createBankRegister(@RequestBody input: ApiCreateBankRegisterModel): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(
            createBankRegister.execAsync(mapApiCreateBankRegister(input))
        )
    }

    @PutMapping("/{id}")
    fun updateBankRegister(@PathVariable id: UUID, @RequestBody input: ApiUpdateBankRegisterModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(
            updateBankRegister.execAsync(mapApiUpdateBankRegister(id, input))
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

    @GetMapping("/institution/{institutionId}")
    fun getBankRegisterByAccessCode(@PathVariable institutionId: String): ResponseEntity<GetBankRegisterOutput> {
        return ResponseEntity.ok(
            getBankRegisterByAccess.execAsync(GetBankRegisterByAccessCodeInput(
                institutionId = institutionId
            ))
        )
    }
}
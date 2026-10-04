package dev.auguste.rest_api.controllers

import adapters.dto.QueryFilter
import dev.auguste.rest_api.controllers.models.ApiCreateBankRegisterModel
import dev.auguste.rest_api.controllers.models.ApiSecureBankRegisterOutput
import dev.auguste.rest_api.controllers.models.ApiUpdateBankRegisterModel
import dev.auguste.rest_api.controllers.models.mapApiCreateBankRegister
import dev.auguste.rest_api.controllers.models.mapApiUpdateBankRegister
import dev.auguste.rest_api.controllers.models.mapBankRegisterToSecure
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
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
    suspend fun createBankRegister(@RequestBody input: ApiCreateBankRegisterModel): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(
            createBankRegister.execute(mapApiCreateBankRegister(input)).getOrThrow()
        )
    }

    @PutMapping("/{id}")
    suspend fun updateBankRegister(@PathVariable id: UUID, @RequestBody input: ApiUpdateBankRegisterModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(
            updateBankRegister.execute(mapApiUpdateBankRegister(id, input)).getOrThrow()
        )
    }

    @GetMapping("/agent-level")
    suspend fun getAllBankRegisterAgentLevel(query: QueryFilter): ResponseEntity<ListOutput<GetBankRegisterOutput>> {
        return ResponseEntity.ok(
            getAllBankRegisters.execute(query).getOrThrow()
        )
    }

    @GetMapping
    suspend fun getAllBankRegister(query: QueryFilter): ResponseEntity<ListOutput<ApiSecureBankRegisterOutput>> {
        val result = getAllBankRegisters.execute(query).getOrThrow()
        return ResponseEntity.ok(
            ListOutput(
                items= result.items.map { mapBankRegisterToSecure(it) },
                total=result.total
            )
        )
    }

    @GetMapping("/institution/{institutionId}")
    suspend fun getBankRegisterByAccessCode(@PathVariable institutionId: String): ResponseEntity<GetBankRegisterOutput> {
        return ResponseEntity.ok(
            getBankRegisterByAccess.execute(GetBankRegisterByAccessCodeInput(
                institutionId = institutionId
            )).getOrThrow()
        )
    }
}
package dev.auguste.rest_api.controllers

import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import configs.PlaidAuth
import dev.auguste.rest_api.controllers.models.ApiCreateBankRegisterModel
import dev.auguste.rest_api.controllers.models.ApiSecureBankRegisterOutput
import dev.auguste.rest_api.controllers.models.ApiUpdateBankRegisterModel
import dev.auguste.rest_api.controllers.models.PlaidAccessCodeRequest
import dev.auguste.rest_api.controllers.models.PlaidAccessCodeResponse
import dev.auguste.rest_api.controllers.models.PlaidExchangePublicTokenResponse
import dev.auguste.rest_api.controllers.models.mapApiCreateBankRegister
import dev.auguste.rest_api.controllers.models.mapApiUpdateBankRegister
import dev.auguste.rest_api.controllers.models.mapBankRegisterToSecure
import org.springframework.beans.factory.annotation.Qualifier
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
import usecases.bank_registers.SyncBankTransactionByBankRegister
import usecases.bank_registers.dto.GetAllBankRegisterInput
import usecases.bank_registers.dto.SyncBankTransactionInput
import java.util.UUID

@RestController
@RequestMapping("/v2/bank-registers")
class BankRegisterController(
    private val plaidAuth: PlaidAuth,
    private val createBankRegister: IUseCase<CreateBankRegisterInput, CreatedOutput>,
    private val updateBankRegister: IUseCase<UpdateBankRegisterInput, Unit>,
    private val getAllBankRegisters: IUseCase<GetAllBankRegisterInput, ListOutput<GetBankRegisterOutput>>,
    private val getBankRegisterByAccess: IUseCase<GetBankRegisterByAccessCodeInput, GetBankRegisterOutput>,
    private val syncBankTransaction: IUseCase<SyncBankTransactionInput, Unit>,
    @Qualifier("syncBankTransactionsByBankRegister")
    private val syncBankTransactionByBankRegister: IUseCase<UUID, Unit>,
    private val auth: IAuthenticateAdapter,
) {

    @PostMapping("/create-bank-linker")
    suspend fun createBankLinker(): ResponseEntity<PlaidExchangePublicTokenResponse> {
        val currentUser = auth.getCurrentUser()
        val token = plaidAuth.createLinkToken(currentUser.id).getOrThrow()
        return ResponseEntity.ok(PlaidExchangePublicTokenResponse(token))
    }

    @PostMapping("/exchange-public-token")
    suspend fun exchangePublicToken(@RequestBody request: PlaidAccessCodeRequest): ResponseEntity<PlaidAccessCodeResponse> {
        auth.getCurrentUser()
        val accessCode = plaidAuth.tokenExchangeRequest(publicToken = request.publicToken).getOrThrow()
        return ResponseEntity.ok(PlaidAccessCodeResponse(accessCode))
    }

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

    @GetMapping
    suspend fun getAllBankRegister(query: QueryFilter): ResponseEntity<ListOutput<ApiSecureBankRegisterOutput>> {
        val result = getAllBankRegisters.execute(GetAllBankRegisterInput(query)).getOrThrow()
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

    @PostMapping("sync-bank-transactions")
    suspend fun syncBankTransactions(): ResponseEntity<Unit> {
        val currentUser = auth.getCurrentUser()
        return ResponseEntity.ok(syncBankTransaction.execute(SyncBankTransactionInput(currentUser.id)).getOrThrow())
    }

    @PostMapping("{id}/sync-bank-transactions")
    suspend fun syncBankTransactionsByBankRegister(@PathVariable id: UUID): ResponseEntity<Unit> {
        return ResponseEntity.ok(syncBankTransactionByBankRegister.execute(id).getOrThrow())
    }
}
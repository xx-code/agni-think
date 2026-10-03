package dev.auguste.rest_api.controllers

import adapters.dto.QueryFilter
import dev.auguste.rest_api.controllers.models.ApiCreateAccountModel
import dev.auguste.rest_api.controllers.models.ApiUpdateAccountModel
import dev.auguste.rest_api.controllers.models.mapApiCreateAccountModel
import dev.auguste.rest_api.controllers.models.mapApiUpdateModel
import usecases.dto.ListOutput
import usecases.accounts.dto.DeleteAccountInput
import usecases.accounts.dto.GetAccountOutput
import usecases.accounts.dto.GetAccountWithDetailOutput
import usecases.accounts.dto.UpdateAccountInput
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import usecases.dto.CreatedOutput
import usecases.accounts.dto.CreateAccountInput
import usecases.interfaces.IUseCase
import java.util.UUID
import kotlin.getOrThrow

@RestController
@RequestMapping("/v2/accounts")
class AccountController(
    private val createAccountUseCase: IUseCase<CreateAccountInput, CreatedOutput>,
    private val updateAccountUseCase: IUseCase<UpdateAccountInput, Unit>,
    private val getAccountUseCase: IUseCase<UUID, GetAccountOutput>,
    private val getAllAccountUseCase: IUseCase<QueryFilter, ListOutput<GetAccountOutput>>,
    private val getAccountWithDetailUseCase: IUseCase<UUID, GetAccountWithDetailOutput>,
    private val getAllAccountWithDetailUseCase: IUseCase<QueryFilter, ListOutput<GetAccountWithDetailOutput>>,
    private val deleteAccountUseCase: IUseCase<DeleteAccountInput, Unit>,
) {

    @GetMapping
    suspend fun getAccounts(queryFilter: QueryFilter, withDetail: Boolean = false): ResponseEntity<ListOutput<*>> {
        val res = if (withDetail)  {
            getAllAccountWithDetailUseCase.execute(queryFilter)
        } else {
            getAllAccountUseCase.execute(queryFilter)
        }

        return ResponseEntity.ok(res.getOrThrow())
    }

    @GetMapping("/{id}")
    suspend fun getAccount(@PathVariable id: UUID, withDetail: Boolean = false): ResponseEntity<*> {
        if (withDetail) {
            return ResponseEntity.ok(getAccountWithDetailUseCase.execute(id).getOrThrow())
        } else {
            return ResponseEntity.ok(getAccountUseCase.execute(id).getOrThrow())
        }
    }

    @PostMapping
    suspend fun createAccount(@Valid @RequestBody request: ApiCreateAccountModel): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createAccountUseCase.execute(mapApiCreateAccountModel(request)).getOrThrow())
    }

    @PutMapping("/{id}")
    suspend fun updateAccount(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpdateAccountModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateAccountUseCase.execute(mapApiUpdateModel(id, request)).getOrThrow())
    }

    @DeleteMapping("/{id}")
    suspend fun deleteAccount(@PathVariable id: UUID) : ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteAccountUseCase.execute(
            DeleteAccountInput(id)
        ).getOrThrow())
    }
}
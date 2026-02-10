package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiCreateAccountModel
import dev.auguste.agni_api.controllers.models.ApiUpdateAccountModel
import dev.auguste.agni_api.controllers.models.mapApiCreateAccountModel
import dev.auguste.agni_api.controllers.models.mapApiUpdateModel
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.accounts.dto.CreateAccountInput
import dev.auguste.agni_api.core.usecases.accounts.dto.GetAccountOutput
import dev.auguste.agni_api.core.usecases.accounts.dto.GetAccountWithDetailOutput
import dev.auguste.agni_api.core.usecases.accounts.dto.UpdateAccountInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import jakarta.validation.Valid
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
@RequestMapping("/v2/accounts")
class AccountController(
    private val createAccountUseCase: IUseCase<CreateAccountInput, CreatedOutput>,
    private val updateAccountUseCase: IUseCase<UpdateAccountInput, Unit>,
    private val getAccountUseCase: IUseCase<UUID, GetAccountOutput>,
    private val getAllAccountUseCase: IUseCase<QueryFilter, ListOutput<GetAccountOutput>>,
    private val getAccountWithDetailUseCase: IUseCase<UUID, GetAccountWithDetailOutput>,
    private val getAllAccountWithDetailUseCase: IUseCase<QueryFilter, ListOutput<GetAccountWithDetailOutput>>,
    private val deleteAccountUseCase: IUseCase<UUID, Unit>,
) {

    @GetMapping
    fun getAccounts(queryFilter: QueryFilter): ResponseEntity<ListOutput<GetAccountOutput>> {
        val res = getAllAccountUseCase.execAsync(queryFilter)
        return ResponseEntity.ok(res)
    }

    @GetMapping("/with-detail")
    fun getAccountsWithDetail(queryFilter: QueryFilter): ResponseEntity<ListOutput<GetAccountWithDetailOutput>>  {
        val res = getAllAccountWithDetailUseCase.execAsync(queryFilter)
        return ResponseEntity.ok(res)
    }

    @GetMapping("/{id}")
    fun getAccount(@PathVariable id: UUID): ResponseEntity<GetAccountOutput> {
       return ResponseEntity.ok(getAccountUseCase.execAsync(id))
    }

    @GetMapping("/with-detail/{id}")
    fun getAccountWithDetail(@PathVariable id: UUID): ResponseEntity<GetAccountWithDetailOutput>  {
        return ResponseEntity.ok(getAccountWithDetailUseCase.execAsync(id))
    }

    @PostMapping
    fun createAccount(@Valid @RequestBody request: ApiCreateAccountModel): ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(createAccountUseCase.execAsync(mapApiCreateAccountModel(request)))
    }

    @PutMapping("/{id}")
    fun updateAccount(@PathVariable id: UUID, @Valid @RequestBody request: ApiUpdateAccountModel): ResponseEntity<Unit> {
        return ResponseEntity.ok(updateAccountUseCase.execAsync(mapApiUpdateModel(id, request)))
    }

    @DeleteMapping("/{id}")
    fun deleteAccount(@PathVariable id: UUID) : ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteAccountUseCase.execAsync(id))
    }
}